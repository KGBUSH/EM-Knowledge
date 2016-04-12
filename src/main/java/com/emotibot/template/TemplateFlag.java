package com.emotibot.template;

public enum TemplateFlag {

	TV_seriesTemplate(0), 
	animeTemplate(1), 
	catchwordTemplate(2), 
	collegeTemplate(3), 
	computer_gameTemplate(4), 
	cosmeticsTemplate(5), 
	delicacyTemplate(6), 
	digital_productTemplate(7), 
	economyTemplate(8), 
	figureTemplate(9), 
	listTemplate(10), 
	majorTemplate(11), 
	medical_treatmentTemplate(12), 
	movieTemplate(13), 
	novelTemplate(14), 
	sportsTemplate(15), 
	sports_organizationTemplate(16), 
	tourismTemplate(17), 
	varity_showTemplate(18);
	
	private int v;

	TemplateFlag(int v) {
		this.v = v; 
	}

	public int getValue() {
		return this.v;
	}

}
