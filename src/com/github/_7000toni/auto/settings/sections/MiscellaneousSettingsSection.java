package com.github._7000toni.auto.settings.sections;

import com.github._7000toni.auto.settings.MiscellaneousSettings;

public class MiscellaneousSettingsSection implements SettingsSection {
	public static final String SECTION_NAME = "MISCELLANEOUS_SETTINGS";
	
	@Override
	public void setSettings(String section) {
		String dataPath = SettingsSectionHelper.setting(section, "DATA_PATH");
		MiscellaneousSettings.setDataPath(SettingsSectionHelper.settingValue(dataPath));
		String arcW = SettingsSectionHelper.setting(section, "ARC_WIDTH");
		MiscellaneousSettings.setArcW(Double.parseDouble(SettingsSectionHelper.settingValue(arcW)));
		String arcH = SettingsSectionHelper.setting(section, "ARC_HEIGHT");
		MiscellaneousSettings.setArcH(Double.parseDouble(SettingsSectionHelper.settingValue(arcH)));
		String tradeButOff = SettingsSectionHelper.setting(section, "TRADE_BUTTON_OFFSET");
		MiscellaneousSettings.setTradeButtonOffset(Double.parseDouble(SettingsSectionHelper.settingValue(tradeButOff)));
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
