package me.lorenzop.webauctionplus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class waSettings {

	protected HashMap<String, String> settingsMap = new HashMap<String, String>();
	protected ItemStack[] itemBlacklist = null;

	private final WebAuctionPlus plugin;
	private boolean isOk;

	public waSettings(WebAuctionPlus plugin) {
		this.plugin = plugin;
		this.isOk = false;
	}


	public synchronized void LoadSettings(){
		this.isOk = false;
		Connection conn = WebAuctionPlus.dataQueries.getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: LoadSettings");
			st = conn.prepareStatement("SELECT `name`, `value` FROM `"+WebAuctionPlus.dataQueries.dbPrefix()+"Settings`");
			rs = st.executeQuery();
			while (rs.next()) {
				if(rs.getString(1) != null)
					this.settingsMap.put(rs.getString(1), rs.getString(2));
			}
			updateSettingsTable();
		} catch (SQLException e) {
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Unable to get settings");
			e.printStackTrace();
			return;
		} finally {
			WebAuctionPlus.dataQueries.closeResources(conn, st, rs);
		}
		addDefaults();
		WebAuctionPlus.log.info(WebAuctionPlus.logPrefix + "Loaded " + Integer.toString(this.settingsMap.size()) + " settings from db");
		this.isOk = (this.settingsMap.size()!=0);
	}
	public boolean isOk() {return this.isOk;}


	// set default settings
	private void addDefaults() {
		addDefault("Version",            this.plugin.getDescription().getVersion().toString());
		addDefault("Language",           "en");
		addDefault("Require Login",      false);
		addDefault("CSRF Protection",    true);
		addDefault("Currency Prefix",    "$ ");
		addDefault("Currency Postfix",   "");
		addDefault("Custom Description", false);
		addDefault("Inventory Rows",     6);
		addDefault("Website Theme",      "");
		addDefault("jQuery UI Pack",     "");
		addDefault("Item Packs",         "");
		addDefault("Max Sell Price",     10000.00);
//		addDefault("Max Selling Per Player",20);
//		addDefault("Storage base per stack",1.0);
//		addDefault("Storage add per item",  0.1);
		addDefault("Item Blacklist",     "");
	}
	private void addDefault(String name, String value) {
		if(!this.settingsMap.containsKey(name)) {
//			if (plugin.dataQueries.debugSQL) WebAuctionPlus.log.info("WA Query: Insert setting: " + name);
			WebAuctionPlus.log.info(WebAuctionPlus.logPrefix + "Adding default setting for: " + name);
			Connection conn = WebAuctionPlus.dataQueries.getConnection();
			PreparedStatement st = null;
			ResultSet rs = null;
			try {
				st = conn.prepareStatement("INSERT INTO `"+WebAuctionPlus.dataQueries.dbPrefix()+"Settings` (`name`,`value`) VALUES (?, ?)");
				st.setString(1, name);
				st.setString(2, value);
				st.executeUpdate();
				this.settingsMap.put(name, value);
			} catch (SQLException e) {
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Unable to add setting: " + name);
				e.printStackTrace();
			} finally {
				WebAuctionPlus.dataQueries.closeResources(conn, st, rs);
			}
		}
	}
	private void addDefault(String name, boolean value) {
		if(value) addDefault(name, "true");
		else      addDefault(name, "false");
	}
	private void addDefault(String name, int value) {
		addDefault(name, Integer.toString(value));
	}
//	private void addDefault(String name, long value) {
//		addDefault(name, Long.toString(value));
//	}
	private void addDefault(String name, double value) {
		addDefault(name, Double.toString(value));
	}


	private void updateSettingsTable() {
		if(this.settingsMap.containsKey("jquery ui pack")) {
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Updating Settings table: jQuery UI Pack");
			WebAuctionPlus.dataQueries.executeRawSQL("UPDATE `"+WebAuctionPlus.dataQueries.dbPrefix()+"Settings` SET `name` = 'jQuery UI Pack' WHERE `name` = 'jquery ui pack' LIMIT 1");
			this.settingsMap.put("jQuery UI Pack", "");
		}
	}


	// get setting
	public synchronized String getString(String name) {
		if(this.settingsMap.containsKey(name))
			return this.settingsMap.get(name);
		else
			return null;
	}
	public boolean getBoolean(String name) {
		String value = this.getString(name);
		if(     value.equalsIgnoreCase("true"))  return true;
		else if(value.equalsIgnoreCase("false")) return false;
		else if(value.equalsIgnoreCase("on"))    return true;
		else if(value.equalsIgnoreCase("off"))   return false;
		else                                     return Boolean.valueOf(value);
	}
	public int getInteger(String name) {
		return Integer.valueOf(this.getString(name));
	}
	public double getDouble(String name) {
		return Double.valueOf(this.getString(name));
	}


	// change setting
	public synchronized void setString(String name, String value) {
		if(!this.settingsMap.containsKey(name)) {
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Setting not found! "+name);
			return;
		}
		if(this.settingsMap.get(name).equals(value)) {
			WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"Setting unchanged, matches existing. "+name);
			return;
		}
		this.settingsMap.put(name, value);
		Connection conn = WebAuctionPlus.dataQueries.getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		if (WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: Update setting: " + name);
		try {
			st = conn.prepareStatement("UPDATE `"+WebAuctionPlus.dataQueries.dbPrefix()+"Settings` SET `value` = ? WHERE `name` = ? LIMIT 1");
			st.setString(1, value);
			st.setString(2, name);
			st.executeUpdate();
		} catch(SQLException e) {
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Unable to update setting " + name);
			e.printStackTrace();
		} finally {
			WebAuctionPlus.dataQueries.closeResources(conn, st, rs);
		}
	}
	public void setInteger(String name, int value) {
		this.setString(name, Integer.toString(value));
	}
	public void setBoolean(String name, boolean value) {
		this.setString(name, Boolean.toString(value));
	}



	public ItemStack[] getItemBlacklist() {
		if(this.itemBlacklist != null)
			return this.itemBlacklist;
		final String str = this.getString("Item Blacklist");
		if(str == null || str.isEmpty()) {
			this.itemBlacklist = new ItemStack[0];
			return this.itemBlacklist;
		}
		final List<ItemStack> list = new ArrayList<ItemStack>();
		final String parts[] = str.split(",");
		for(String part : parts) {
			part = part.trim();
			if(part.isEmpty()) continue;
			final ItemStack stack = getItemBlacklist_Type(part);
			if(stack == null) continue;
			list.add(stack);
		}
		this.itemBlacklist = list.toArray(new ItemStack[0]);
		return this.itemBlacklist;
	}
	@SuppressWarnings("deprecation")
	private static ItemStack getItemBlacklist_Type(final String str) {
		if(str == null || str.isEmpty())
			return null;
		final String typeStr;
		Material mat;
		final short damage;
		if(str.contains(":")) {
			final String[] p = str.split(":");
			typeStr = p[0];
			try {
				damage = Short.parseShort(p[1]);
			} catch (NumberFormatException e) {
				System.out.println("Invalid backlisted item: "+str);
				e.printStackTrace();
				return null;
			}
		} else {
			typeStr = str;
			damage = -1;
		}
		try {
			final int id = Integer.parseInt(typeStr);
			mat = Material.getMaterial(id);
		} catch (NumberFormatException e) {
			System.out.println("Invalid backlisted item: "+str);
			e.printStackTrace();
			return null;
			// don't use this, can't be supported on php side
//			try {
//				mat = Material.valueOf(typeStr);
//			} catch (IllegalArgumentException e2) {
//				System.out.println("Invalid backlisted item: "+str);
//				e2.printStackTrace();
//				return null;
//			}
		}
		if(damage == -1)
			return new ItemStack(mat, 1);
		return new ItemStack(mat, 1, damage);
	}



}
