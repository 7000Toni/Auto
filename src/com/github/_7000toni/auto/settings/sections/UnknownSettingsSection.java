package com.github._7000toni.auto.settings.sections;

import com.github._7000toni.auto.settings.MiscellaneousSettings;

public class UnknownSettingsSection extends SettingsSection {
	public static final String SECTION_NAME = "MISCELLANEOUS_SETTINGS";
	
	@Override
	public void setSettings(String section) {
		String dataPath = setting(section, "DATA_PATH");
		MiscellaneousSettings.setDataPath(settingValue(dataPath));
		String arcW = setting(section, "ARC_WIDTH");
		MiscellaneousSettings.setArcW(Double.parseDouble(settingValue(arcW)));
		String arcH = setting(section, "ARC_HEIGHT");
		MiscellaneousSettings.setArcH(Double.parseDouble(settingValue(arcH)));
		String tradeButOff = setting(section, "TRADE_BUTTON_OFFSET");
		MiscellaneousSettings.setTradeButtonOffset(Double.parseDouble(settingValue(tradeButOff)));
	}
	
	@Override
	public String defaultSettings() {
		return MiscellaneousSettings.defaultSettings();
	}
	
	@Override
	public String currentSettings() {
		return MiscellaneousSettings.string();
	}
}
