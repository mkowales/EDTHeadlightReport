package com.edt.b4t.smartsheet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.edt.b4t.B4TProjects;
import com.edt.b4t.B4TUsers;
import com.edt.b4t.util.AppProperties;
import com.edt.b4t.util.Str;
import com.edt.b4t.util.TimeDate;
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Cell;
import com.smartsheet.api.models.Column;
import com.smartsheet.api.models.Row;
import com.smartsheet.api.models.Sheet;

public class EDTHeadlight
{
	private B4TProjects projects = null;
	
	private B4TUsers users = null; 
	
	private Smartsheet smartsheet = null;
	
	private Sheet sheet = null;
	
	private String projectHeading = "",
					sheetIdProp = "",
					endDate = "";
	
// The API identifies columns by Id, but it's more convenient to refer to column names
    private static HashMap<String, Long> columnMap = new HashMap<String, Long>();   // Map from friendly column name to column Id

	private List<Cell> cells;
	
	public static void main(String[] args)
			throws NumberFormatException, SmartsheetException, IOException
	{
		EDTHeadlight headlighter = new EDTHeadlight();
		
		int index = 0,
			closedRecords = 0,
			projectSize = headlighter.projects.getCustomIds().size();
		
		ArrayList<EDTHeadlightProps> headlights = new ArrayList<EDTHeadlightProps>();
        
		System.out.println("Searching " + projectSize + " projects");
		
		for (index = 0; index < projectSize; index++)
		{
			String projectId = headlighter.projects.getCustomIds().get(index),
					clientName = headlighter.projects.getClientNames().get(index),
					project = headlighter.projects.getProjectNames().get(index),
					projectType = headlighter.projects.getProjectTypes().get(index),
					serviceArea = null;
			
			if ((clientName.equals("Presales and Prospecting")) || 
					(project.equals("Recurring Subscriptions")) ||
					(null == (serviceArea = headlighter.convertType(projectType))) ||
					(projectType.equals("Maintenance and Support")))
				continue;
			
			if (!headlighter.projectIdFound(projectId))
			{
				EDTHeadlightProps headlight = new EDTHeadlightProps();
				
				headlight.setClientName(clientName);
				headlight.setProjectId(projectId);
				headlight.setProjectName(project);
				headlight.setServiceArea(serviceArea);
				headlight.setProjectOwner(headlighter.getUserName(headlighter.projects.getAssignedTos().get(index)));
				
				headlights.add(headlight);
			}
		}
		
		B4TProjects closedProjects = headlighter.getCompletedProjects();
		
		System.out.println("Checking " + closedProjects.getIds().size() + " closed projects");
		
		ArrayList<EDTHeadlightProps> lightsOff = new ArrayList<EDTHeadlightProps>();
		
		for (int element = 0; element < closedProjects.getIds().size(); element++)
		{
			Row updateRow = null;
			
			if ((headlighter.projectIdFound(closedProjects.getIds().get(element))) &&
					(null != (updateRow = headlighter.findRow(closedProjects.getIds().get(element)))))
			{
				if (headlighter.completeProject(updateRow))
				{
					closedRecords += headlighter.closeProject(updateRow.getId(), closedProjects.getIds().get(element));
					
					System.out.println("Completed project " + closedProjects.getIds().get(element));
					
					EDTHeadlightProps lightOff = new EDTHeadlightProps();
					
					lightOff.setClientName(closedProjects.getClientNames().get(element));
					lightOff.setProjectId(closedProjects.getIds().get(element));
					lightOff.setProjectName(closedProjects.getProjectNames().get(element));
					lightOff.setServiceArea(closedProjects.getProjectTypes().get(element));
					lightOff.setProjectOwner(headlighter.getUserName(closedProjects.getAssignedTos().get(element)));
					
					lightsOff.add(lightOff);
				}
			}
		}
			
		try
		{
			for (index = 0; index < headlights.size(); index++)
				headlighter.addRow(headlights.get(index));
			
			if ((0 < index) || (0 < closedRecords))
			{
				String body = "";
				
				EDTHeadlightEmail email = new EDTHeadlightEmail();
			
				if (0 < index)
					body += email.getBody(headlights, "EagleDream Technologies added " + headlights.size() + " record(s) to the Headlight Report", true);
			
				if (0 < closedRecords)
					body += email.getBody(lightsOff, "EagleDream Technologies completed "
					        + lightsOff.size() + " record(s) in the Headlight Report", (0 == body.length()));
				
				email.send(body);
			}
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		
	    System.out.println("\t- " + index + " records added");
	    System.out.println("\t- " + closedRecords + " records closed");
	    
		System.out.println("Done!!");
	}

	public EDTHeadlight()
	{
		AppProperties.setFname("props/EDTHeadlight");
		AppProperties.init();
		
		int days = Integer.valueOf(System.getProperty("days"));

		SimpleDateFormat format = new SimpleDateFormat();
		format = new SimpleDateFormat("YYYY-MM-dd");
        
        this.endDate = format.format(TimeDate.today());
        
		String projPrefs = "?$filter=createdDate ge '" + 
						TimeDate.convertDate(TimeDate.todayMinus(days)) + "'",
				userPrefs = "?select=id,fname,lname";
		
		this.projects = new B4TProjects(Str.convertToURL(projPrefs));
		this.users = new B4TUsers(userPrefs);
		
		String accessTokenProp = System.getProperty("accessToken");
		
		this.projectHeading = System.getProperty("project.heading");
		this.sheetIdProp = System.getProperty("sheetId");
		
		System.out.println(
				"accessToken = " + accessTokenProp
				+ "; sheetId = " + this.sheetIdProp
				);
		
		try
		{
	        if (accessTokenProp == null || accessTokenProp.isEmpty())
	            throw new Exception("Must set API access token in rwsheet.properties file");
	        
	        // Initialize client
	        this.smartsheet = new SmartsheetBuilder().setAccessToken(accessTokenProp).build();
	        
	        // Load the entire sheet
	        this.sheet = this.smartsheet.sheetResources().getSheet(Long.parseLong(sheetIdProp), 
	        			null, null, null, null, null, null, null);
	        
	         for (Column column : this.sheet.getColumns())
	             EDTHeadlight.columnMap.put(column.getTitle(), column.getId());
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
	}

	private Row findRow(String projectId)
	{
		Row updateRow = null;
		
        for (Row row : this.sheet.getRows())
        {
            Cell cell = this.findColumnCell(row, this.projectHeading);

            if (null == cell.getDisplayValue())
            	break;
            
            if (cell.getDisplayValue().equals(projectId))
            {
            	updateRow = row;
            	break;
            }
        }

		return updateRow;
	}
	
	private boolean projectIdFound(String projectId)
	{
		boolean found = false;
		
        for (Row row : this.sheet.getRows())
        {
            Cell cell = this.findColumnCell(row, this.projectHeading);

            if (null == cell.getDisplayValue())
            	break;
            
            if (found = cell.getDisplayValue().equals(projectId))
            	break;
        }

		return found;
	}
	
    private Cell findColumnCell(Row row, String columnName)
    {
        Long colId = EDTHeadlight.columnMap.get(columnName);

        return row.getCells().stream().filter(cell -> colId.equals((Long)cell.getColumnId())).findFirst().orElse(null);
    }

    private boolean completeProject(Row row) 
    {
 // 	Find cell we want to examine
    	Cell statusCell = this.findColumnCell(row, "Activity");
    	
//    	System.out.println(statusCell.getDisplayValue() + " " + !"Complete".equals(statusCell.getDisplayValue()));

		 return (!"Complete".equals(statusCell.getDisplayValue()));
 	}
 
    private String getUserName(String id)
    {
    	boolean found = false;
    	
    	String name = "";
    	
    	for (int index = 0; (!found) && (index < this.users.getIds().size()); index++)
    	{
    		found = this.users.getIds().get(index).equals(id);
    		
//    		System.out.println("FOUND = " + found + " @ " + index + " for " + id);
    		
    		if (found)
    			name = this.users.getFnames().get(index) + " " + this.users.getLnames().get(index);
    	}
    	
    	return (name);
    }
    
    private int addRow(EDTHeadlightProps headlight) 
    		throws NumberFormatException, SmartsheetException
    {
    	int index = 0;
    	
    	this.cells = null;
    	
        for (Column column : this.sheet.getColumns())
        {
            switch (index)
            {
            	case 0:
            		System.out.println(column.getId() + " " + column.getTitle() + " = " + headlight.getProjectId());
            		
            		if (null == this.cells)
            			this.cells = new Cell.AddRowCellsBuilder().addCell(column.getId(), headlight.getProjectId()).build();
            		else
            			this.cells.addAll(new Cell.AddRowCellsBuilder().addCell(column.getId(), headlight.getProjectId()).build());
            		break;
            		
            	case 1:
            		System.out.println(column.getId() + " " + column.getTitle() + " = " + headlight.getClientName());
            		this.cells.addAll(new Cell.AddRowCellsBuilder().addCell(column.getId(), headlight.getClientName()).build());
            		break;
            		
            	case 2:
            		System.out.println(column.getId() + " " + column.getTitle() + " = " + headlight.getProjectName());
            		this.cells.addAll(new Cell.AddRowCellsBuilder().addCell(column.getId(), headlight.getProjectName()).build());
            		break;
            		
            	case 3:
            		System.out.println(column.getId() + " " + column.getTitle() + " = " + headlight.getServiceArea());
            		this.cells.addAll(new Cell.AddRowCellsBuilder().addCell(column.getId(), headlight.getServiceArea()).build());
            		break;
            		
            	case 4:
            		System.out.println(column.getId() + " " + column.getId() + " " + column.getTitle() + " = " + headlight.getStatus());
            		this.cells.addAll(new Cell.AddRowCellsBuilder().addCell(column.getId(), headlight.getStatus()).build());
            		break;
            		
            	case 5:
            		System.out.println(column.getId() + " " + column.getTitle() + " = " + headlight.getActivity());
            		this.cells.addAll(new Cell.AddRowCellsBuilder().addCell(column.getId(), headlight.getActivity()).build());
            		break;
            		
            	case 6:
            		System.out.println(column.getId() + " " + column.getTitle() + " = " + headlight.getStatusNotes());
            		this.cells.addAll(new Cell.AddRowCellsBuilder().addCell(column.getId(), headlight.getStatusNotes()).build());
            		break;
            		
            	case 8:
            		System.out.println(column.getId() + " " + column.getTitle() + " = " + headlight.getStartDate());
//            		set the value as true and then pass it a string of YYYY-MM-dd
            		this.cells.addAll(new Cell.AddRowCellsBuilder().addCell(column.getId(), headlight.getStartDate()).build());
            		break;
            		
            	case 11:
            		System.out.println(column.getId() + " " + column.getTitle() + " = " + headlight.getProjectOwner());
            		this.cells.addAll(new Cell.AddRowCellsBuilder().addCell(column.getId(), headlight.getProjectOwner()).build());
            		break;
            		
            	default:
            		System.out.println(index + 
            					". " + column.getTitle() + 
            					" --> " + column.getId() +
            					" " + column.getType());
            		break;
            }
            
            index++;
        }
        
// Specify contents of first row
    	Row row = new Row.AddRowBuilder().setCells(this.cells).setToBottom(true).build();
    	
// Add rows to sheet
    	return (this.smartsheet.sheetResources().rowResources().addRows(Long.valueOf(this.sheetIdProp), Arrays.asList(row)).size());
    }
    
	
	private String convertType(String projectType)
	{
		String serviceArea = null;
		
		switch (projectType)
		{
			case "Advisory":
			case "Assessment":
			case "Managed Services":
			case "Project Management":
				serviceArea = "Consulting";
				break;
				
			case "Development, General":
			case "Development, Product":
				serviceArea = "Application Development";
				break;
				
			case "Development, Security":
				serviceArea = "IT Security";
				break;
				
			case "Development, Web":
				serviceArea = "Web Development";
				break;
				
			case "Infrastructure":
				serviceArea = "Cloud/Infrastructure";
				break;
				
			case "Support":
				serviceArea = "Maintenance and Support";
				break;
				
			case "Internal":
				break;
		}
		
		return (serviceArea);
	}

	public B4TProjects getCompletedProjects()
	{
		String prefs = "?$filter=status eq 'Closed'";
		
		B4TProjects projects = new B4TProjects(Str.convertToURL(prefs));
		
		return (projects);
	}
	
	public int closeProject(long rowId, String id)
			throws NumberFormatException, SmartsheetException
	{
    	int index = 0;
    	
    	final String activityComplete = "Complete";
    	
    	String statusNote = "EDT automation completed record on ";
    	
        statusNote = statusNote + this.endDate;

		this.cells = null;
		
		System.out.println("Closing " + id);
    	
        for (Column column : this.sheet.getColumns())
        {
            switch (index)
            {
            	case 5:
            		System.out.println(column.getId() + " " + column.getTitle() + " = " + activityComplete);

            		if (null == this.cells)
            			this.cells = new Cell.UpdateRowCellsBuilder().addCell(column.getId(), activityComplete).build();
            		else
            			this.cells.addAll(new Cell.UpdateRowCellsBuilder().addCell(column.getId(), activityComplete).build());
            		break;
            		
            	case 6:
            		System.out.println(column.getId() + " " + column.getTitle() + " = " + statusNote);
            		this.cells.addAll(new Cell.UpdateRowCellsBuilder().addCell(column.getId(), statusNote).build());
            		break;
            		
            	case 9:
            		System.out.println(column.getId() + " " + column.getTitle() + " = " + this.endDate);
//            		set the value as true and then pass it a string of YYYY-MM-dd
            		this.cells.addAll(new Cell.UpdateRowCellsBuilder().addCell(column.getId(), this.endDate).build());
            		break;
            		
            	default:
//            		System.out.println(index + 
//            					". " + column.getTitle() + 
//            					" --> " + column.getId() +
//            					" " + column.getType());
            		break;
            }
            
            index++;
        }
        
// Specify contents of first row
    	Row row = new Row.UpdateRowBuilder().setCells(this.cells).setRowId(rowId).build();
    	
// Add rows to sheet
    	List<Row> updatedRows = this.smartsheet.sheetResources().rowResources()
    			.updateRows(Long.valueOf(this.sheetIdProp), Arrays.asList(row));
    	
    	return (updatedRows.size());
	}
}
