package com.github.mrebhan.ingameaccountswitcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.github.mrebhan.ingameaccountswitcher.events.FMLEvents;
import com.github.mrebhan.ingameaccountswitcher.tools.Config;
import com.github.mrebhan.ingameaccountswitcher.tools.Tools;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Session;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
/**
 * @author mrebhan
 * @author The_Fireplace
 */
@Mod(modid=IngameAccountSwitcher.MODID, name=IngameAccountSwitcher.MODNAME, version=IngameAccountSwitcher.VERSION, clientSideOnly=true)
public class IngameAccountSwitcher {
	@Instance(value=IngameAccountSwitcher.MODID)
	public static IngameAccountSwitcher instance;
	//Moved these here so they can be called from wherever in the mod they are needed
	public static final String MODID = "IngameAccountSwitcher";
	public static final String MODNAME = "In-game Account Switcher";
	public static final String VERSION = "2.0.1.0";
	public static String releaseVersion = "";
	public static String prereleaseVersion = "";
	public static final String downloadURL = "http://goo.gl/1erpBM";
	//For Dynious's Version Checker
	public static NBTTagCompound update = new NBTTagCompound();
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.load();
		FMLCommonHandler.instance().bus().register(new FMLEvents());
		retriveCurrentVersions();
		this.addUpdateInfo(update, this.MODNAME, this.VERSION, this.prereleaseVersion, this.releaseVersion, this.downloadURL, this.MODID);
	}
	
	public static void setSession(Session s) throws Exception {
		Class<? extends Minecraft> mc = Minecraft.getMinecraft().getClass();
		try {
			Field session = null;
			
			for (Field f : mc.getDeclaredFields()) {
				if (f.getType().isInstance(s)) {
					session = f;
					System.out.println("Found field " + f.toString() + ", injecting...");
				}
			}
			
			if (session == null) {
				throw new IllegalStateException("No field of type " + Session.class.getCanonicalName() + " declared.");
			}
			
			session.setAccessible(true);
			session.set(Minecraft.getMinecraft(), s);
			session.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Retrieves what the latest version is from Dropbox
	 */
	private static void retriveCurrentVersions() {
		try {
			releaseVersion = get_content(new URL(
					"https://dl.dropboxusercontent.com/s/l2i7ua5u4j5i8sc/release.version?dl=0")
					.openConnection());

			prereleaseVersion = get_content(new URL(
					"https://dl.dropboxusercontent.com/s/55rwhwvai453yqz/prerelease.version?dl=0")
					.openConnection());

		} catch (final MalformedURLException e) {
			System.out.println("Malformed URL Exception");
			releaseVersion = "";
			prereleaseVersion = "";
		} catch (final IOException e) {
			System.out.println("IO Exception");
			releaseVersion = "";
			prereleaseVersion = "";
		}
	}

	private static String get_content(URLConnection con) throws IOException {
		String output = "";

		if (con != null) {
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					con.getInputStream()));

			String input;

			while ((input = br.readLine()) != null) {
				output = output + input;
			}
			br.close();
		}

		return output;
	}
	/**
	 * Makes update info available to Version Checker
	 * @param updateInfo
	 * 		The NBT Tag the information is stored in
	 * @param modDisplayName
	 * 		The mod's display name
	 * @param oldVersion
	 * 		The mod's current version
	 * @param newPreVersion
	 * 		The latest pre-release version
	 * @param newVersion
	 * 		The latest release version
	 * @param updateURL
	 * 		The URL users need to go to in order to get the latest version
	 * @param modid
	 * 		The modid
	 */
	public static void addUpdateInfo(NBTTagCompound updateInfo, String modDisplayName, String oldVersion, String newPreVersion, String newVersion, String updateURL, String modid){
		String versiontoshow;
		if (!newVersion.equals("") && !newPreVersion.equals("")) {//Prevents crashing if the connection to the server failed.
			if(Tools.isHigherVersion(newVersion, newPreVersion)){
			versiontoshow = newPreVersion;
			}else{
			versiontoshow = newVersion;
			}
		}else{
			versiontoshow = "0.0.0.0";
		}
		updateInfo.setString("modDisplayName", modDisplayName);
		updateInfo.setString("oldVersion", oldVersion);
		updateInfo.setString("newVersion", versiontoshow);
		updateInfo.setString("updateURL", updateURL);
		updateInfo.setBoolean("isDirectLink", false);
		if(Tools.isHigherVersion(oldVersion, versiontoshow))
		FMLInterModComms.sendRuntimeMessage(modid, "VersionChecker", "addUpdate", updateInfo);
	}
}