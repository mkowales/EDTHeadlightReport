package com.edt.b4t.smartsheet;

import java.text.SimpleDateFormat;

import com.edt.b4t.util.TimeDate;

public class EDTHeadlightProps
{
	private String 
			projectId = "",
			clientName = "",
			projectName = "",
			serviceArea = "",
			status = "Green",
			activity = "In Progress",
			statusNotes = "EDT automation created record on ",
			
			startDate = "",
			projectOwner = "";

	public EDTHeadlightProps()
	{
		SimpleDateFormat format = new SimpleDateFormat();
//		format = new SimpleDateFormat("MM/dd/yy");
		format = new SimpleDateFormat("YYYY-MM-dd");
        
        this.startDate = format.format(TimeDate.today());
        this.statusNotes = this.statusNotes + this.startDate;
	}

	public String getProjectId()
	{
		return projectId;
	}

	public void setProjectId(String projectId)
	{
		this.projectId = projectId;
	}

	public String getProjectName()
	{
		return this.projectName;
	}

	public void setProjectName(String projectName)
	{
		this.projectName = projectName;
	}

	public String getClientName()
	{
		return this.clientName;
	}

	public void setClientName(String clientName)
	{
		this.clientName = clientName;
	}

	public String getServiceArea()
	{
		return this.serviceArea;
	}

	public void setServiceArea(String serviceArea)
	{
		this.serviceArea = serviceArea;
	}

	public String getProjectOwner()
	{
		return this.projectOwner;
	}

	public void setProjectOwner(String projectOwner)
	{
		this.projectOwner = projectOwner;
	}

	public String getActivity()
	{
		return this.activity;
	}

	public String getStatusNotes()
	{
		return this.statusNotes;
	}

	public String getStartDate()
	{
		return this.startDate;
	}

	public String getStatus()
	{
		return this.status;
	}
}
