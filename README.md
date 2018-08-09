# EDTHeadlightReport

EDTHeadlightReport is an application that runs once every week. It calculates all projects created in Bill4Time the previous week, adds them to SmartSheet & disables the projects that were closed in Bill4Time.

It’s executed using “crontab“ & uses property configuration settings from the EDTHeadlightReport file.

It’s written in Java & requires access to these libraries:
* EDTB4TUtil
* EDT365Email
* SmartSheet
