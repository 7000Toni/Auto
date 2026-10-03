package com.github._7000toni.auto.settings.sections;

import com.github._7000toni.auto.settings.MiscellaneousSettings;

public class MiscellaneousSection implements SettingsSection {
	public static final String SECTION_NAME = "[MISCELLANEOUS_SETTINGS]";
	
	@Override
	public void setSettings(String section) {
		if (section == null) {
			return;
		}
		
		String dataPath = SettingsSectionHelper.setting(section, "DATA_PATH");
		if (dataPath != null) {
			MiscellaneousSettings.setDataPath(SettingsSectionHelper.settingValue(dataPath));
		}
		String arcW = SettingsSectionHelper.setting(section, "ARC_WIDTH");
		try {
			if (arcW != null) {
				MiscellaneousSettings.setArcW(Double.parseDouble(SettingsSectionHelper.settingValue(arcW)));
			}
			String arcH = SettingsSectionHelper.setting(section, "ARC_HEIGHT");
			if (arcH != null) {
				MiscellaneousSettings.setArcH(Double.parseDouble(SettingsSectionHelper.settingValue(arcH)));
			}
			String tradeButOff = SettingsSectionHelper.setting(section, "TRADE_BUTTON_OFFSET");
			if (tradeButOff != null) {
				MiscellaneousSettings.setTradeButtonOffset(Double.parseDouble(SettingsSectionHelper.settingValue(tradeButOff)));
			}
		} catch (NumberFormatException | NullPointerException e) {
			e.printStackTrace();
		}
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
