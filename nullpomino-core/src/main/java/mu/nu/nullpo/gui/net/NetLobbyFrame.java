/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/
package mu.nu.nullpo.gui.net;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Locale;
import java.util.zip.Adler32;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.net.NetBaseClient;
import mu.nu.nullpo.game.net.NetMessageListener;
import mu.nu.nullpo.game.net.NetPlayerClient;
import mu.nu.nullpo.game.net.NetPlayerInfo;
import mu.nu.nullpo.game.net.NetRoomInfo;
import mu.nu.nullpo.game.net.NetUtil;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.mode.NetDummyMode;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * NullpoMino NetLobby
 */
public class NetLobbyFrame extends JFrame implements ActionListener, NetMessageListener {
	/** Serial Version ID */
	private static final long serialVersionUID = 1L;

	/** Room-table column names. These strings will be passed to getUIText(String) subroutine. */
	public static final String[] ROOMTABLE_COLUMNNAMES = {
		"RoomTable_ID","RoomTable_Name","RoomTable_Rated","RoomTable_RuleName","RoomTable_ModeName",
		"RoomTable_Status","RoomTable_Players","RoomTable_Spectators"
	};

	/** End-of-game statistics column names. These strings will be passed to getUIText(String) subroutine. */
	public static final String[] STATTABLE_COLUMNNAMES = {
		"StatTable_Rank","StatTable_Name",
		"StatTable_Attack","StatTable_APL","StatTable_APM","StatTable_Lines","StatTable_LPM","StatTable_Piece","StatTable_PPS","StatTable_Time",
		"StatTable_KO","StatTable_Wins","StatTable_Games"
	};

	/** 1P end-of-game statistics column names. These strings will be passed to getUIText(String) subroutine. */
	public static final String[] STATTABLE1P_COLUMNNAMES = {
		"StatTable1P_Description", "StatTable1P_Value"
	};

	/** Multiplayer leaderboard column names. These strings will be passed to getUIText(String) subroutine. */
	public static final String[] MPRANKING_COLUMNNAMES  = {
		"MPRanking_Rank", "MPRanking_Name", "MPRanking_Rating", "MPRanking_PlayCount", "MPRanking_WinCount"
	};

	/** Tuning: Generic Auto/Disable/Enable labels (before translation) */
	public static final String[] TUNING_COMBOBOX_GENERIC = {
		"GameTuning_ComboboxGeneric_Auto", "GameTuning_ComboboxGeneric_Disable", "GameTuning_ComboboxGeneric_Enable"
	};

	/** Tuning: A button rotation (before translation) */
	public static final String[] TUNING_ABUTTON_ROTATE = {
		"GameTuning_RotateButtonDefaultRight_Auto", "GameTuning_RotateButtonDefaultRight_Left", "GameTuning_RotateButtonDefaultRight_Right"
	};

	/** Tuning: Outline type names (before translation) */
	public static final String[] TUNING_OUTLINE_TYPE_NAMES = {
		"GameTuning_OutlineType_Auto", "GameTuning_OutlineType_None", "GameTuning_OutlineType_Normal",
		"GameTuning_OutlineType_Connect", "GameTuning_OutlineType_SameColor"
	};

	/** Spin bonus names */
	public static final String[] COMBOBOX_SPINBONUS_NAMES = {"CreateRoom_TSpin_Disable", "CreateRoom_TSpin_TOnly", "CreateRoom_TSpin_All"};

	/** Names for spin check types */
	public static final String[] COMBOBOX_SPINCHECKTYPE_NAMES = {"CreateRoom_SpinCheck_4Point", "CreateRoom_SpinCheck_Immobile"};

	/** Constants for each screen-card */
	public static final int SCREENCARD_SERVERSELECT = 0,
							SCREENCARD_LOBBY = 1,
							SCREENCARD_SERVERADD = 2,
							SCREENCARD_CREATERATED_WAITING = 3,
							SCREENCARD_CREATERATED = 4,
							SCREENCARD_CREATEROOM = 5,
							SCREENCARD_CREATEROOM1P = 6,
							SCREENCARD_MPRANKING = 7,
							SCREENCARD_RULECHANGE = 8;

	/** Names for each screen-card */
	public static final String[] SCREENCARD_NAMES = {"ServerSelect","Lobby","ServerAdd",
		"CreateRatedWaiting","CreateRated","CreateRoom","CreateRoom1P","MPRanking","RuleChange"};

	/** Log */
	static final Logger log = Logger.getLogger(NetLobbyFrame.class);

	/** NetPlayerClient */
	public NetPlayerClient netPlayerClient;

	/** Rule data */
	public RuleOptions ruleOptPlayer, ruleOptLock;

	/** Map list */
	public LinkedList<String> mapList;

	/** Event listeners */
	protected LinkedList<NetLobbyListener> listeners = new LinkedList<NetLobbyListener>();

	/** Preset info */
	protected LinkedList<NetRoomInfo> presets = new LinkedList<NetRoomInfo>();

	/** Current game mode (act as special NetLobbyListener) */
	protected NetDummyMode netDummyMode;

	/** Property file for lobby settings */
	protected CustomProperties propConfig;

	/** Property file for global settings */
	protected CustomProperties propGlobal;

	/** Property file for swing settings */
	protected CustomProperties propSwingConfig;

	/** Property file for observer ("Watch") settings */
	protected CustomProperties propObserver;

	/** Default game mode description file */
	protected CustomProperties propDefaultModeDesc;

	/** Game mode description file */
	protected CustomProperties propModeDesc;

	/** Default language file */
	protected CustomProperties propLangDefault;

	/** Property file for GUI translations */
	protected CustomProperties propLang;

	/** Current screen-card number */
	protected int currentScreenCardNumber;

	/** Current room ID (for View Detail) */
	protected int currentViewDetailRoomID = -1;

	/** NetRoomInfo for settings backup */
	protected NetRoomInfo backupRoomInfo;

	/** NetRoomInfo for settings backup (1P) */
	protected NetRoomInfo backupRoomInfo1P;

	/** PrintWriter for lobby log */
	protected PrintWriter writerLobbyLog;

	/** PrintWriter for room log */
	protected PrintWriter writerRoomLog;

	/** Rated-game rule name list */
	protected LinkedList<String>[] listRatedRuleName;

	/** Layout manager for main screen */
	protected CardLayout contentPaneCardLayout;

	/** Menu bars (all screens) */
	protected JMenuBar[] menuBar;

	/** Text field of player name (Server select screen) */
	protected JTextField txtfldPlayerName;

	/** Text field of team name (Server select screen) */
	protected JTextField txtfldPlayerTeam;

	/** Listbox for servers (Server select screen) */
	protected JList listboxServerList;

	/** Listbox data for servers (Server select screen) */
	protected DefaultListModel listmodelServerList;

	/** Connect button (Server select screen) */
	protected JButton btnServerConnect;

	/** Lobby/Room Tab */
	protected JTabbedPane tabLobbyAndRoom;

	/** JSplitPane (Lobby screen) */
	protected JSplitPane splitLobby;

	/** At the top of the lobby screen layout manager */
	protected CardLayout roomListTopBarCardLayout;

	/** Panel at the top of the lobby screen */
	protected JPanel subpanelRoomListTopBar;

	/** Lobby popup menu (Lobby screen) */
	protected JPopupMenu popupLobbyOptions;

	/** Rule change menu item (Lobby screen) */
	protected JMenuItem itemLobbyMenuRuleChange;

	/** Team change menu item (Lobby screen) */
	protected JMenuItem itemLobbyMenuTeamChange;

	/** Leaderboard menu item (Lobby screen) */
	protected JMenuItem itemLobbyMenuRanking;

	/** Quick Start button(Lobby screen) */
	protected JButton btnRoomListQuickStart;

	/** Create a Room button(Lobby screen) */
	protected JButton btnRoomListRoomCreate;

	/** Create Room 1P (Lobby screen) */
	protected JButton btnRoomListRoomCreate1P;

	/** Options menu button (Lobby screen) */
	protected JButton btnRoomListOptions;

	/** Team name input Column(Lobby screen) */
	protected JTextField txtfldRoomListTeam;

	/** Room list table */
	protected JTable tableRoomList;

	/** Room list tableのカラム名(翻訳後) */
	protected String[] strTableColumnNames;

	/** Room list tableの data */
	protected DefaultTableModel tablemodelRoomList;

	/** Chat logAndPlayerPartition line of the list(Lobby screen) */
	protected JSplitPane splitLobbyChat;

	/** Chat log(Lobby screen) */
	protected JTextPane txtpaneLobbyChatLog;

	/** PlayerList(Lobby screen) */
	protected JList listboxLobbyChatPlayerList;

	/** PlayerList(Lobby screen)Of data */
	protected DefaultListModel listmodelLobbyChatPlayerList;

	/** Chat input Column(Lobby screen) */
	protected JTextField txtfldLobbyChatInput;

	/** Submit chat button(Lobby screen) */
	protected JButton btnLobbyChatSend;

	/** Participation in a war button(Room screen) */
	protected JButton btnRoomButtonsJoin;

	/** Withdrawal button(Room screen) */
	protected JButton btnRoomButtonsSitOut;

	/** Change team button(Room screen) */
	protected JButton btnRoomButtonsTeamChange;

	/** View Settings button (Room screen) */
	protected JButton btnRoomButtonsViewSetting;

	/** Leaderboard button (Room screen) */
	protected JButton btnRoomButtonsRanking;

	/** Partition line separating the upper and lower(Room screen) */
	protected JSplitPane splitRoom;

	/** Room at the top of the screen layout manager */
	protected CardLayout roomTopBarCardLayout;

	/** Top panel room screen */
	protected JPanel subpanelRoomTopBar;

	/** Game stats panel */
	protected JPanel subpanelGameStat;

	/** CardLayout for game stats */
	protected CardLayout gameStatCardLayout;

	/** Multiplayer game stats table */
	protected JTable tableGameStat;

	/** Multiplayer game stats table column names */
	protected String[] strGameStatTableColumnNames;

	/** Multiplayer game stats table data */
	protected DefaultTableModel tablemodelGameStat;

	/** Multiplayer game stats table */
	protected JTable tableGameStat1P;

	/** Multiplayer game stats table column names */
	protected String[] strGameStatTableColumnNames1P;

	/** Multiplayer game stats table data */
	protected DefaultTableModel tablemodelGameStat1P;

	/** Chat logAndPlayerPartition line of the list(Room screen) */
	protected JSplitPane splitRoomChat;

	/** Chat log(Room screen) */
	protected JTextPane txtpaneRoomChatLog;

	/** PlayerList(Room screen) */
	protected JList listboxRoomChatPlayerList;

	/** PlayerList(Room screen)Of data */
	protected DefaultListModel listmodelRoomChatPlayerList;

	/** The same roomPlayerInformation */
	protected LinkedList<NetPlayerInfo> sameRoomPlayerInfoList;

	/** Chat input Column(Room screen) */
	protected JTextField txtfldRoomChatInput;

	/** Submit chat button(Room screen) */
	protected JButton btnRoomChatSend;

	/** Team name input Column(Room screen) */
	protected JTextField txtfldRoomTeam;

	/** Host name input Column(Server add screen) */
	protected JTextField txtfldServerAddHost;

	/** OK button(Server add screen) */
	protected JButton btnServerAddOK;

	protected JTextField txtfldCreateRatedName;

	/** Cancel button (Created rated waiting screen) */
	protected JButton btnCreateRatedWaitingCancel;

	/** Presets box (Create rated screen) */
	protected JComboBox comboboxCreateRatedPresets;

	/** People participatecount(Create rated screen) */
	protected JSpinner spinnerCreateRatedMaxPlayers;

	/** OK button (Create rated screen) */
	protected JButton btnCreateRatedOK;

	/** Custom button (Create rated screen) */
	protected JButton btnCreateRatedCustom;

	/** Cancel button (Created rated screen) */
	protected JButton btnCreateRatedCancel;

	/** ルーム名(Create room screen) */
	protected JTextField txtfldCreateRoomName;

	/** Game Mode (Create room screen) */
	protected JComboBox comboboxCreateRoomMode;

	/** People participatecount(Create room screen) */
	protected JSpinner spinnerCreateRoomMaxPlayers;

	/** To wait before auto-start time(Create room screen) */
	protected JSpinner spinnerCreateRoomAutoStartSeconds;

	/** Molecular fall velocity(Create room screen) */
	protected JSpinner spinnerCreateRoomGravity;

	/** Denominator-fall velocity(Create room screen) */
	protected JSpinner spinnerCreateRoomDenominator;

	/** ARE(Create room screen) */
	protected JSpinner spinnerCreateRoomARE;

	/** ARE after line clear(Create room screen) */
	protected JSpinner spinnerCreateRoomARELine;

	/** Line clear time(Create room screen) */
	protected JSpinner spinnerCreateRoomLineDelay;

	/** Fixation time(Create room screen) */
	protected JSpinner spinnerCreateRoomLockDelay;

	/** Horizontal reservoir(Create room screen) */
	protected JSpinner spinnerCreateRoomDAS;

	/** HurryupSeconds before the startcount(Create room screen) */
	protected JSpinner spinnerCreateRoomHurryupSeconds;

	/** HurryupTimes afterBlockDo you run up the floor every time you put the(Create room screen) */
	protected JSpinner spinnerCreateRoomHurryupInterval;

	/** MapSetID(Create room screen) */
	protected JSpinner spinnerCreateRoomMapSetID;

	/** Rate of change of garbage holes */
	protected JSpinner spinnerCreateRoomGarbagePercent;

	/** Map is enabled(Create room screen) */
	protected JCheckBox chkboxCreateRoomUseMap;

	/** Of all fixed rules(Create room screen) */
	protected JCheckBox chkboxCreateRoomRuleLock;

	/** Spin bonusType(Create room screen) */
	protected JComboBox comboboxCreateRoomTSpinEnableType;

	/** Spin recognition type (4-point, immobile, etc.) */
	protected JComboBox comboboxCreateRoomSpinCheckType;

	/** Flag for enabling B2B(Create room screen) */
	protected JCheckBox chkboxCreateRoomB2B;

	/** Flag for enabling combos(Create room screen) */
	protected JCheckBox chkboxCreateRoomCombo;

	/** Allow Rensa/Combo Block */
	protected JCheckBox chkboxCreateRoomRensaBlock;

	/** Allow countering */
	protected JCheckBox chkboxCreateRoomCounter;

	/** Enable bravo bonus */
	protected JCheckBox chkboxCreateRoomBravo;

	/** Allow EZ spins */
	protected JCheckBox chkboxCreateRoomTSpinEnableEZ;

	/** 3If I live more than Attack Reduce the force(Create room screen) */
	protected JCheckBox chkboxCreateRoomReduceLineSend;

	/** Set garbage type */
	protected JCheckBox chkboxCreateRoomGarbageChangePerAttack;

	/** Set garbage type */
	protected JCheckBox chkboxCreateRoomDivideChangeRateByPlayers;

	/** B2B chunk type */
	protected JCheckBox chkboxCreateRoomB2BChunk;

	/** Fragmentarygarbage blockUsing the system(Create room screen) */
	protected JCheckBox chkboxCreateRoomUseFractionalGarbage;

	/** Use Target System (Create room screen) */
	protected JCheckBox chkboxCreateRoomIsTarget;

	/** Spinner for Targeting time (Create room screen) */
	protected JSpinner spinnerCreateRoomTargetTimer;

	/** TNET2TypeAutomatically start timerI use(Create room screen) */
	protected JCheckBox chkboxCreateRoomAutoStartTNET2;

	/** SomeoneCancelWasTimerInvalidation(Create room screen) */
	protected JCheckBox chkboxCreateRoomDisableTimerAfterSomeoneCancelled;

	/** Preset number (Create room screen) */
	protected JSpinner spinnerCreateRoomPresetID;

	/** Preset code (Create room screen) */
	protected JTextField txtfldCreateRoomPresetCode;

	/** OK button(Create room screen) */
	protected JButton btnCreateRoomOK;

	/** Participation in a war button(Create room screen) */
	protected JButton btnCreateRoomJoin;

	/** Spectator button(Create room screen) */
	protected JButton btnCreateRoomWatch;

	/** Cancel Button (Create room screen) */
	protected JButton btnCreateRoomCancel;

	/** Game mode label (Create room 1P screen) */
	protected JLabel labelCreateRoom1PGameMode;

	/** Game mode listbox (Create room 1P screen) */
	protected JList listboxCreateRoom1PModeList;

	/** Game mode list data (Create room 1P screen) */
	protected DefaultListModel listmodelCreateRoom1PModeList;

	/** Rule list listbox (Create room 1P screen) */
	protected JList listboxCreateRoom1PRuleList;

	/** Rule list list data (Create room 1P screen) */
	protected DefaultListModel listmodelCreateRoom1PRuleList;

	/** OK button (Create room 1P screen) */
	protected JButton btnCreateRoom1POK;

	/** Cancel button (Create room 1P screen) */
	protected JButton btnCreateRoom1PCancel;

	/** Tab (MPRanking screen) */
	protected JTabbedPane tabMPRanking;

	/** Column names of multiplayer leaderboard (MPRanking screen) */
	protected String[] strMPRankingTableColumnNames;

	/** Table of multiplayer leaderboard (MPRanking screen) */
	protected JTable[] tableMPRanking;

	/** Table data of multiplayer leaderboard (MPRanking screen) */
	protected DefaultTableModel[] tablemodelMPRanking;

	/** OK button (MPRanking screen) */
	protected JButton btnMPRankingOK;

	/** Tab (Rule change screen) */
	protected JTabbedPane tabRuleChange;

	/** Rule list listbox (Rule change screen) */
	protected JList[] listboxRuleChangeRuleList;

	/** OK button (Rule change screen) */
	protected JButton btnRuleChangeOK;

	/** Cancel button (Rule change screen) */
	protected JButton btnRuleChangeCancel;

	/** Rule entries (Rule change screen) */
	protected LinkedList<RuleEntry> ruleEntries;

	/** Tuning: A button rotation Combobox */
	protected JComboBox comboboxTuningRotateButtonDefaultRight;
	/** Tuning: Diagonal move Combobox */
	protected JComboBox comboboxTuningMoveDiagonal;
	/** Tuning: Show Outline Only Combobox */
	protected JComboBox comboboxTuningBlockShowOutlineOnly;
	/** Tuning: Skin Combobox */
	protected JComboBox comboboxTuningSkin;
	/** Tuning: Skin Images */
	protected BufferedImage[] imgTuningBlockSkins;
	/** Tuning: Outline type combobox */
	protected JComboBox comboboxTuningBlockOutlineType;
	/** Tuning: Minimum DAS */
	protected JTextField txtfldTuningMinDAS;
	/** Tuning: Maximum DAS */
	protected JTextField txtfldTuningMaxDAS;
	/** Tuning: DAS dealy */
	protected JTextField txtfldTuningDasDelay;
	/** Tuning: Checkbox to enable swapping the roles of up/down buttons in-game */
	protected JCheckBox chkboxTuningReverseUpDown;

	/**
	 * Constructor
	 */
	public NetLobbyFrame() {
		super();
	}

	/**
	 * Initialization
	 */
	public void init() {
		// Read configuration file
		propConfig = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/setting/netlobby.cfg");
			propConfig.load(in);
			in.close();
		} catch(IOException e) {}

		// Load global settings
		propGlobal = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/setting/global.cfg");
			propGlobal.load(in);
			in.close();
		} catch(IOException e) {}

		// SwingRead version of the configuration file
		propSwingConfig = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/setting/swing.cfg");
			propSwingConfig.load(in);
			in.close();
		} catch(IOException e) {}

		// ObserverFunction read configuration file
		propObserver = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/setting/netobserver.cfg");
			propObserver.load(in);
			in.close();
		} catch(IOException e) {}

		// Game mode description
		propDefaultModeDesc = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/lang/modedesc_default.properties");
			propDefaultModeDesc.load(in);
			in.close();
		} catch(IOException e) {
			log.error("Couldn't load default mode description file", e);
		}
		propModeDesc = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/lang/modedesc_" + Locale.getDefault().getCountry() + ".properties");
			propModeDesc.load(in);
			in.close();
		} catch(IOException e) {}

		// Read language file
		propLangDefault = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/lang/netlobby_default.properties");
			propLangDefault.load(in);
			in.close();
		} catch (Exception e) {
			log.error("Couldn't load default UI language file", e);
		}

		propLang = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/lang/netlobby_" + Locale.getDefault().getCountry() + ".properties");
			propLang.load(in);
			in.close();
		} catch(IOException e) {}

		// Look&FeelSetting
		if(propSwingConfig.getProperty("option.usenativelookandfeel", true) == true) {
			try {
				UIManager.getInstalledLookAndFeels();
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch(Exception e) {
				log.warn("Failed to set native look&feel", e);
			}
		}

		// WindowListenerSign up
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				shutdown();
			}
		});

		// Rated-game rule name list
		listRatedRuleName = new LinkedList[GameEngine.MAX_GAMESTYLE];
		for(int i = 0; i < GameEngine.MAX_GAMESTYLE; i++) {
			listRatedRuleName[i] = new LinkedList<String>();
		}

		// Map list
		mapList = new LinkedList<String>();

		// Rule files
		String[] strRuleFileList = getRuleFileList();
		if(strRuleFileList == null) {
			log.error("Rule file directory not found");
		} else {
			createRuleEntries(strRuleFileList);
		}

		// Block skins
		loadBlockSkins();

		// GUI Init
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setTitle(getUIText("Title_NetLobby"));

		initUI();

		this.setSize(propConfig.getProperty("mainwindow.width", 500), propConfig.getProperty("mainwindow.height", 450));
		this.setLocation(propConfig.getProperty("mainwindow.x", 0), propConfig.getProperty("mainwindow.y", 0));

		// ListenerCall
		if(listeners != null) {
			for(NetLobbyListener l: listeners) {
				if(l != null)
					l.netlobbyOnInit(this);
			}
		}
		if(netDummyMode != null) {
			netDummyMode.netlobbyOnInit(this);
		}
	}

	/**
	 * GUI Initialization
	 */
	protected void initUI() {
		contentPaneCardLayout = new CardLayout();
		this.getContentPane().setLayout(contentPaneCardLayout);

		menuBar = new JMenuBar[SCREENCARD_NAMES.length];
		for (int i = 0; i < SCREENCARD_NAMES.length; i++) {
			menuBar[i] = new JMenuBar();
		}

		initServerSelectUI();
		initLobbyUI();
		initServerAddUI();
		initCreateRatedWaitingUI();
		initCreateRatedUI();
		initCreateRoomUI();
		initCreateRoom1PUI();
		initMPRankingUI();
		initRuleChangeUI();

		changeCurrentScreenCard(SCREENCARD_SERVERSELECT);
	}

	/**
	 * Server select screen initialization
	 */
	protected void initServerSelectUI() {
		// And server selectionName input Screen
		JPanel mainpanelServerSelect = new JPanel(new BorderLayout());
		this.getContentPane().add(mainpanelServerSelect, SCREENCARD_NAMES[SCREENCARD_SERVERSELECT]);

		// * NameAndTeam name input Panel
		JPanel subpanelNames = new JPanel();
		subpanelNames.setLayout(new BoxLayout(subpanelNames, BoxLayout.Y_AXIS));
		mainpanelServerSelect.add(subpanelNames, BorderLayout.NORTH);

		// ** Name input Panel
		JPanel subpanelNameEntry = new JPanel(new BorderLayout());
		subpanelNames.add(subpanelNameEntry);

		// *** &#39;Name:&quot;Label
		JLabel labelNameEntry = new JLabel(getUIText("ServerSelect_LabelName"));
		subpanelNameEntry.add(labelNameEntry, BorderLayout.WEST);

		// *** Name input Column
		txtfldPlayerName = new JTextField();
		txtfldPlayerName.setComponentPopupMenu(new TextComponentPopupMenu(txtfldPlayerName));
		txtfldPlayerName.setText(propConfig.getProperty("serverselect.txtfldPlayerName.text", ""));
		subpanelNameEntry.add(txtfldPlayerName, BorderLayout.CENTER);

		// ** Team name input Panel
		JPanel subpanelTeamEntry = new JPanel(new BorderLayout());
		subpanelNames.add(subpanelTeamEntry);

		// *** &#39;Team name:&quot;Label
		JLabel labelTeamEntry = new JLabel(getUIText("ServerSelect_LabelTeam"));
		subpanelTeamEntry.add(labelTeamEntry, BorderLayout.WEST);

		// *** Team name input Column
		txtfldPlayerTeam = new JTextField();
		txtfldPlayerTeam.setComponentPopupMenu(new TextComponentPopupMenu(txtfldPlayerTeam));
		txtfldPlayerTeam.setText(propConfig.getProperty("serverselect.txtfldPlayerTeam.text", ""));
		subpanelTeamEntry.add(txtfldPlayerTeam, BorderLayout.CENTER);

		// * Server selection list box
		listmodelServerList = new DefaultListModel();
		if(GameManager.isDevBuild()) {
			if(!loadListToDefaultListModel(listmodelServerList, "config/setting/netlobby_serverlist_dev.cfg")) {
				loadListToDefaultListModel(listmodelServerList, "config/list/netlobby_serverlist_default_dev.lst");
				saveListFromDefaultListModel(listmodelServerList, "config/setting/netlobby_serverlist_dev.cfg");
			}
		} else {
			if(!loadListToDefaultListModel(listmodelServerList, "config/setting/netlobby_serverlist.cfg")) {
				loadListToDefaultListModel(listmodelServerList, "config/list/netlobby_serverlist_default.lst");
				saveListFromDefaultListModel(listmodelServerList, "config/setting/netlobby_serverlist.cfg");
			}
		}
		listboxServerList = new JList(listmodelServerList);
		listboxServerList.setComponentPopupMenu(new ServerSelectListBoxPopupMenu());
		listboxServerList.addMouseListener(new ServerSelectListBoxMouseAdapter());
		listboxServerList.setSelectedValue(propConfig.getProperty("serverselect.listboxServerList.value", ""), true);
		JScrollPane spListboxServerSelect = new JScrollPane(listboxServerList);
		mainpanelServerSelect.add(spListboxServerSelect, BorderLayout.CENTER);

		// * Panel add or remove server
		JPanel subpanelServerAdd = new JPanel();
		subpanelServerAdd.setLayout(new BoxLayout(subpanelServerAdd, BoxLayout.Y_AXIS));
		mainpanelServerSelect.add(subpanelServerAdd, BorderLayout.EAST);

		// ** Add Server button
		JButton btnServerAdd = new JButton(getUIText("ServerSelect_ServerAdd"));
		btnServerAdd.setMaximumSize(new Dimension(Short.MAX_VALUE, btnServerAdd.getMaximumSize().height));
		btnServerAdd.addActionListener(this);
		btnServerAdd.setActionCommand("ServerSelect_ServerAdd");
		btnServerAdd.setMnemonic('A');
		subpanelServerAdd.add(btnServerAdd);

		// ** Delete server button
		JButton btnServerDelete = new JButton(getUIText("ServerSelect_ServerDelete"));
		btnServerDelete.setMaximumSize(new Dimension(Short.MAX_VALUE, btnServerDelete.getMaximumSize().height));
		btnServerDelete.addActionListener(this);
		btnServerDelete.setActionCommand("ServerSelect_ServerDelete");
		btnServerDelete.setMnemonic('D');
		subpanelServerAdd.add(btnServerDelete);

		// ** Monitoring settings button
		JButton btnSetObserver = new JButton(getUIText("ServerSelect_SetObserver"));
		btnSetObserver.setMaximumSize(new Dimension(Short.MAX_VALUE, btnSetObserver.getMaximumSize().height));
		btnSetObserver.addActionListener(this);
		btnSetObserver.setActionCommand("ServerSelect_SetObserver");
		btnSetObserver.setMnemonic('S');
		subpanelServerAdd.add(btnSetObserver);

		// ** Unmonitor button
		JButton btnUnsetObserver = new JButton(getUIText("ServerSelect_UnsetObserver"));
		btnUnsetObserver.setMaximumSize(new Dimension(Short.MAX_VALUE, btnUnsetObserver.getMaximumSize().height));
		btnUnsetObserver.addActionListener(this);
		btnUnsetObserver.setActionCommand("ServerSelect_UnsetObserver");
		btnUnsetObserver.setMnemonic('U');
		subpanelServerAdd.add(btnUnsetObserver);

		// * Connection button·Exit buttonPanel
		JPanel subpanelServerSelectButtons = new JPanel();
		subpanelServerSelectButtons.setLayout(new BoxLayout(subpanelServerSelectButtons, BoxLayout.X_AXIS));
		mainpanelServerSelect.add(subpanelServerSelectButtons, BorderLayout.SOUTH);

		// ** Connection button
		btnServerConnect = new JButton(getUIText("ServerSelect_Connect"));
		btnServerConnect.setMaximumSize(new Dimension(Short.MAX_VALUE, btnServerConnect.getMaximumSize().height));
		btnServerConnect.addActionListener(this);
		btnServerConnect.setActionCommand("ServerSelect_Connect");
		btnServerConnect.setMnemonic('C');
		subpanelServerSelectButtons.add(btnServerConnect);

		// ** Exit button
		JButton btnServerExit = new JButton(getUIText("ServerSelect_Exit"));
		btnServerExit.setMaximumSize(new Dimension(Short.MAX_VALUE, btnServerExit.getMaximumSize().height));
		btnServerExit.addActionListener(this);
		btnServerExit.setActionCommand("ServerSelect_Exit");
		btnServerExit.setMnemonic('X');
		subpanelServerSelectButtons.add(btnServerExit);
	}

	/**
	 * Lobby screen initialization
	 */
	protected void initLobbyUI() {
		tabLobbyAndRoom = new JTabbedPane();
		this.getContentPane().add(tabLobbyAndRoom, SCREENCARD_NAMES[SCREENCARD_LOBBY]);

		// === Popup Menu ===

		// * Popup Menu
		popupLobbyOptions = new JPopupMenu();

		// ** Rule change
		itemLobbyMenuRuleChange = new JMenuItem(getUIText("Lobby_RuleChange"));
		itemLobbyMenuRuleChange.addActionListener(this);
		itemLobbyMenuRuleChange.setActionCommand("Lobby_RuleChange");
		itemLobbyMenuRuleChange.setMnemonic('R');
		itemLobbyMenuRuleChange.setToolTipText(getUIText("Lobby_RuleChange_Tip"));
		popupLobbyOptions.add(itemLobbyMenuRuleChange);

		// ** Team change
		itemLobbyMenuTeamChange = new JMenuItem(getUIText("Lobby_TeamChange"));
		itemLobbyMenuTeamChange.addActionListener(this);
		itemLobbyMenuTeamChange.setActionCommand("Lobby_TeamChange");
		itemLobbyMenuTeamChange.setMnemonic('T');
		itemLobbyMenuTeamChange.setToolTipText(getUIText("Lobby_TeamChange_Tip"));
		popupLobbyOptions.add(itemLobbyMenuTeamChange);

		// ** Leaderboard
		itemLobbyMenuRanking = new JMenuItem(getUIText("Lobby_Ranking"));
		itemLobbyMenuRanking.addActionListener(this);
		itemLobbyMenuRanking.setActionCommand("Lobby_Ranking");
		itemLobbyMenuRanking.setMnemonic('K');
		itemLobbyMenuRanking.setToolTipText(getUIText("Lobby_Ranking_Tip"));
		popupLobbyOptions.add(itemLobbyMenuRanking);

		// === Lobby Tab ===
		JPanel mainpanelLobby = new JPanel(new BorderLayout());
		//this.getContentPane().add(mainpanelLobby, SCREENCARD_NAMES[SCREENCARD_LOBBY]);
		tabLobbyAndRoom.addTab(getUIText("Lobby_Tab_Lobby"), mainpanelLobby);
		tabLobbyAndRoom.setMnemonicAt(0, 'Y');

		// * Partition line separating the upper and lower
		splitLobby = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitLobby.setDividerLocation(propConfig.getProperty("lobby.splitLobby.location", 200));
		mainpanelLobby.add(splitLobby, BorderLayout.CENTER);

		// ** Room list(Top)
		JPanel subpanelRoomList = new JPanel(new BorderLayout());
		subpanelRoomList.setMinimumSize(new Dimension(0,0));
		splitLobby.setTopComponent(subpanelRoomList);

		// *** Top of the screen panel lobby
		roomListTopBarCardLayout = new CardLayout();
		subpanelRoomListTopBar = new JPanel(roomListTopBarCardLayout);
		subpanelRoomList.add(subpanelRoomListTopBar, BorderLayout.NORTH);

		// **** Room list buttonKind
		JPanel subpanelRoomListButtons = new JPanel();
		subpanelRoomListTopBar.add(subpanelRoomListButtons, "Buttons");
		//subpanelRoomList.add(subpanelRoomListButtons, BorderLayout.NORTH);

		// ***** TODO:Quick Start button
		btnRoomListQuickStart = new JButton(getUIText("Lobby_QuickStart"));
		btnRoomListQuickStart.addActionListener(this);
		btnRoomListQuickStart.setActionCommand("Lobby_QuickStart");
		btnRoomListQuickStart.setMnemonic('Q');
		btnRoomListQuickStart.setToolTipText(getUIText("Lobby_QuickStart_Tip"));
		btnRoomListQuickStart.setVisible(false);
		subpanelRoomListButtons.add(btnRoomListQuickStart);

		// ***** Create a Room button
		btnRoomListRoomCreate = new JButton(getUIText("Lobby_RoomCreate"));
		btnRoomListRoomCreate.addActionListener(this);
		btnRoomListRoomCreate.setActionCommand("Lobby_RoomCreate");
		btnRoomListRoomCreate.setMnemonic('N');
		btnRoomListRoomCreate.setToolTipText(getUIText("Lobby_RoomCreate_Tip"));
		subpanelRoomListButtons.add(btnRoomListRoomCreate);

		// ***** Create Room (1P) button
		btnRoomListRoomCreate1P = new JButton(getUIText("Lobby_RoomCreate1P"));
		btnRoomListRoomCreate1P.addActionListener(this);
		btnRoomListRoomCreate1P.setActionCommand("Lobby_RoomCreate1P");
		btnRoomListRoomCreate1P.setMnemonic('1');
		btnRoomListRoomCreate1P.setToolTipText(getUIText("Lobby_RoomCreate1P_Tip"));
		subpanelRoomListButtons.add(btnRoomListRoomCreate1P);

		// ***** Options menu button
		btnRoomListOptions = new JButton(getUIText("Lobby_Options"));
		btnRoomListOptions.addActionListener(this);
		btnRoomListOptions.setActionCommand("Lobby_Options");
		btnRoomListOptions.setMnemonic('O');
		btnRoomListOptions.setToolTipText(getUIText("Lobby_Options_Tip"));
		subpanelRoomListButtons.add(btnRoomListOptions);

		// ***** Cut button
		JButton btnRoomListDisconnect = new JButton(getUIText("Lobby_Disconnect"));
		btnRoomListDisconnect.addActionListener(this);
		btnRoomListDisconnect.setActionCommand("Lobby_Disconnect");
		btnRoomListDisconnect.setMnemonic('L');
		btnRoomListDisconnect.setToolTipText(getUIText("Lobby_Disconnect_Tip"));
		subpanelRoomListButtons.add(btnRoomListDisconnect);

		// **** Panel change team
		JPanel subpanelRoomListTeam = new JPanel(new BorderLayout());
		subpanelRoomListTopBar.add(subpanelRoomListTeam, "Team");

		// ***** Team name input Column
		txtfldRoomListTeam = new JTextField();
		subpanelRoomListTeam.add(txtfldRoomListTeam, BorderLayout.CENTER);

		// ***** Team nameChange buttonPanel
		JPanel subpanelRoomListTeamButtons = new JPanel();
		subpanelRoomListTeam.add(subpanelRoomListTeamButtons, BorderLayout.EAST);

		// ****** Team nameChangeOK
		JButton btnRoomListTeamOK = new JButton(getUIText("Lobby_TeamChange_OK"));
		btnRoomListTeamOK.addActionListener(this);
		btnRoomListTeamOK.setActionCommand("Lobby_TeamChange_OK");
		btnRoomListTeamOK.setMnemonic('O');
		subpanelRoomListTeamButtons.add(btnRoomListTeamOK);

		// ****** Team nameChangeCancel
		JButton btnRoomListTeamCancel = new JButton(getUIText("Lobby_TeamChange_Cancel"));
		btnRoomListTeamCancel.addActionListener(this);
		btnRoomListTeamCancel.setActionCommand("Lobby_TeamChange_Cancel");
		btnRoomListTeamCancel.setMnemonic('C');
		subpanelRoomListTeamButtons.add(btnRoomListTeamCancel);

		// *** Room list table
		strTableColumnNames = new String[ROOMTABLE_COLUMNNAMES.length];
		for(int i = 0; i < strTableColumnNames.length; i++) {
			strTableColumnNames[i] = getUIText(ROOMTABLE_COLUMNNAMES[i]);
		}
		tablemodelRoomList = new DefaultTableModel(strTableColumnNames, 0);
		tableRoomList = new JTable(tablemodelRoomList);
		tableRoomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableRoomList.setDefaultEditor(Object.class, null);
		tableRoomList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableRoomList.getTableHeader().setReorderingAllowed(false);
		tableRoomList.setComponentPopupMenu(new RoomTablePopupMenu());
		tableRoomList.addMouseListener(new RoomTableMouseAdapter());
		tableRoomList.addKeyListener(new RoomTableKeyAdapter());

		TableColumnModel tm = tableRoomList.getColumnModel();
		tm.getColumn(0).setPreferredWidth(propConfig.getProperty("tableRoomList.width.id", 35));			// ID
		tm.getColumn(1).setPreferredWidth(propConfig.getProperty("tableRoomList.width.name", 155));			// Name
		tm.getColumn(2).setPreferredWidth(propConfig.getProperty("tableRoomList.width.rated", 50));			// Rated
		tm.getColumn(3).setPreferredWidth(propConfig.getProperty("tableRoomList.width.rulename", 105));		// Rule name
		tm.getColumn(4).setPreferredWidth(propConfig.getProperty("tableRoomList.width.modename", 105));		// Mode name
		tm.getColumn(5).setPreferredWidth(propConfig.getProperty("tableRoomList.width.status", 55));		// Status
		tm.getColumn(6).setPreferredWidth(propConfig.getProperty("tableRoomList.width.players", 65));		// Players
		tm.getColumn(7).setPreferredWidth(propConfig.getProperty("tableRoomList.width.spectators", 65));	// Spectators

		JScrollPane spTableRoomList = new JScrollPane(tableRoomList);
		subpanelRoomList.add(spTableRoomList, BorderLayout.CENTER);

		// ** Chat(Under)
		JPanel subpanelLobbyChat = new JPanel(new BorderLayout());
		subpanelLobbyChat.setMinimumSize(new Dimension(0,0));
		splitLobby.setBottomComponent(subpanelLobbyChat);

		// *** Chat logAndPlayerPartition line of the list
		splitLobbyChat = new JSplitPane();
		splitLobbyChat.setDividerLocation(propConfig.getProperty("lobby.splitLobbyChat.location", 350));
		subpanelLobbyChat.add(splitLobbyChat, BorderLayout.CENTER);

		// **** Chat log(Lobby screen)
		txtpaneLobbyChatLog = new JTextPane();
		txtpaneLobbyChatLog.setComponentPopupMenu(new LogPopupMenu(txtpaneLobbyChatLog));
		txtpaneLobbyChatLog.addKeyListener(new LogKeyAdapter());
		JScrollPane spTxtpaneLobbyChatLog = new JScrollPane(txtpaneLobbyChatLog);
		spTxtpaneLobbyChatLog.setMinimumSize(new Dimension(0,0));
		splitLobbyChat.setLeftComponent(spTxtpaneLobbyChatLog);

		// **** PlayerList(Lobby screen)
		listmodelLobbyChatPlayerList = new DefaultListModel();
		listboxLobbyChatPlayerList = new JList(listmodelLobbyChatPlayerList);
		listboxLobbyChatPlayerList.setComponentPopupMenu(new ListBoxPopupMenu(listboxLobbyChatPlayerList));
		JScrollPane spListboxLobbyChatPlayerList = new JScrollPane(listboxLobbyChatPlayerList);
		spListboxLobbyChatPlayerList.setMinimumSize(new Dimension(0, 0));
		splitLobbyChat.setRightComponent(spListboxLobbyChatPlayerList);

		// *** Chat input Column panel(Lobby screen)
		JPanel subpanelLobbyChatInputArea = new JPanel(new BorderLayout());
		subpanelLobbyChat.add(subpanelLobbyChatInputArea, BorderLayout.SOUTH);

		// **** Chat input Column(Lobby screen)
		txtfldLobbyChatInput = new JTextField();
		txtfldLobbyChatInput.setComponentPopupMenu(new TextComponentPopupMenu(txtfldLobbyChatInput));
		subpanelLobbyChatInputArea.add(txtfldLobbyChatInput, BorderLayout.CENTER);

		// **** Submit chat button(Lobby screen)
		btnLobbyChatSend = new JButton(getUIText("Lobby_ChatSend"));
		btnLobbyChatSend.addActionListener(this);
		btnLobbyChatSend.setActionCommand("Lobby_ChatSend");
		btnLobbyChatSend.setMnemonic('S');
		subpanelLobbyChatInputArea.add(btnLobbyChatSend, BorderLayout.EAST);

		// === Room Tab ===
		JPanel mainpanelRoom = new JPanel(new BorderLayout());
		//this.getContentPane().add(mainpanelRoom, SCREENCARD_NAMES[SCREENCARD_ROOM]);
		tabLobbyAndRoom.addTab(getUIText("Lobby_Tab_NoRoom"), mainpanelRoom);
		tabLobbyAndRoom.setMnemonicAt(1, 'R');
		tabLobbyAndRoom.setEnabledAt(1, false);

		// * Partition line separating the upper and lower
		splitRoom = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitRoom.setDividerLocation(propConfig.getProperty("room.splitRoom.location", 200));
		mainpanelRoom.add(splitRoom, BorderLayout.CENTER);

		// ** List of game results(Top)
		JPanel subpanelRoomTop = new JPanel(new BorderLayout());
		subpanelRoomTop.setMinimumSize(new Dimension(0,0));
		splitRoom.setTopComponent(subpanelRoomTop);

		// *** Top panel room screen
		roomTopBarCardLayout = new CardLayout();
		subpanelRoomTopBar = new JPanel(roomTopBarCardLayout);
		subpanelRoomTop.add(subpanelRoomTopBar, BorderLayout.NORTH);

		// ****  buttonPanel type
		JPanel subpanelRoomButtons = new JPanel();
		subpanelRoomTopBar.add(subpanelRoomButtons, "Buttons");

		// ***** Withdrawal button
		JButton btnRoomButtonsLeave = new JButton(getUIText("Room_Leave"));
		btnRoomButtonsLeave.addActionListener(this);
		btnRoomButtonsLeave.setActionCommand("Room_Leave");
		btnRoomButtonsLeave.setMnemonic('L');
		btnRoomButtonsLeave.setToolTipText(getUIText("Room_Leave_Tip"));
		subpanelRoomButtons.add(btnRoomButtonsLeave);

		// ***** Participation in a war button
		btnRoomButtonsJoin = new JButton(getUIText("Room_Join"));
		btnRoomButtonsJoin.addActionListener(this);
		btnRoomButtonsJoin.setActionCommand("Room_Join");
		btnRoomButtonsJoin.setMnemonic('J');
		btnRoomButtonsJoin.setToolTipText(getUIText("Room_Join_Tip"));
		btnRoomButtonsJoin.setVisible(false);
		subpanelRoomButtons.add(btnRoomButtonsJoin);

		// ***** Withdrawal button
		btnRoomButtonsSitOut = new JButton(getUIText("Room_SitOut"));
		btnRoomButtonsSitOut.addActionListener(this);
		btnRoomButtonsSitOut.setActionCommand("Room_SitOut");
		btnRoomButtonsSitOut.setMnemonic('W');
		btnRoomButtonsSitOut.setToolTipText(getUIText("Room_SitOut_Tip"));
		btnRoomButtonsSitOut.setVisible(false);
		subpanelRoomButtons.add(btnRoomButtonsSitOut);

		// ***** Change team button
		btnRoomButtonsTeamChange = new JButton(getUIText("Room_TeamChange"));
		btnRoomButtonsTeamChange.addActionListener(this);
		btnRoomButtonsTeamChange.setActionCommand("Room_TeamChange");
		btnRoomButtonsTeamChange.setMnemonic('T');
		btnRoomButtonsTeamChange.setToolTipText(getUIText("Room_TeamChange_Tip"));
		subpanelRoomButtons.add(btnRoomButtonsTeamChange);

		// **** Panel change team
		JPanel subpanelRoomTeam = new JPanel(new BorderLayout());
		subpanelRoomTopBar.add(subpanelRoomTeam, "Team");

		// ***** Team name input Column
		txtfldRoomTeam = new JTextField();
		subpanelRoomTeam.add(txtfldRoomTeam, BorderLayout.CENTER);

		// ***** Team nameChange buttonPanel
		JPanel subpanelRoomTeamButtons = new JPanel();
		subpanelRoomTeam.add(subpanelRoomTeamButtons, BorderLayout.EAST);

		// ****** Team nameChangeOK
		JButton btnRoomTeamOK = new JButton(getUIText("Room_TeamChange_OK"));
		btnRoomTeamOK.addActionListener(this);
		btnRoomTeamOK.setActionCommand("Room_TeamChange_OK");
		btnRoomTeamOK.setMnemonic('O');
		subpanelRoomTeamButtons.add(btnRoomTeamOK);

		// ****** Team nameChangeCancel
		JButton btnRoomTeamCancel = new JButton(getUIText("Room_TeamChange_Cancel"));
		btnRoomTeamCancel.addActionListener(this);
		btnRoomTeamCancel.setActionCommand("Room_TeamChange_Cancel");
		btnRoomTeamCancel.setMnemonic('C');
		subpanelRoomTeamButtons.add(btnRoomTeamCancel);

		// ***** Setting confirmation button
		btnRoomButtonsViewSetting = new JButton(getUIText("Room_ViewSetting"));
		btnRoomButtonsViewSetting.addActionListener(this);
		btnRoomButtonsViewSetting.setActionCommand("Room_ViewSetting");
		btnRoomButtonsViewSetting.setMnemonic('V');
		btnRoomButtonsViewSetting.setToolTipText(getUIText("Room_ViewSetting_Tip"));
		subpanelRoomButtons.add(btnRoomButtonsViewSetting);

		// ***** Leaderboard button
		btnRoomButtonsRanking = new JButton(getUIText("Room_Ranking"));
		btnRoomButtonsRanking.addActionListener(this);
		btnRoomButtonsRanking.setActionCommand("Room_Ranking");
		btnRoomButtonsRanking.setMnemonic('K');
		btnRoomButtonsRanking.setToolTipText(getUIText("Room_Ranking_Tip"));
		btnRoomButtonsRanking.setVisible(false);
		subpanelRoomButtons.add(btnRoomButtonsRanking);

		// *** Game stats area
		gameStatCardLayout = new CardLayout();
		subpanelGameStat = new JPanel(gameStatCardLayout);
		subpanelRoomTop.add(subpanelGameStat, BorderLayout.CENTER);

		// **** Multiplayer game stats table
		strGameStatTableColumnNames = new String[STATTABLE_COLUMNNAMES.length];
		for(int i = 0; i < strGameStatTableColumnNames.length; i++) {
			strGameStatTableColumnNames[i] = getUIText(STATTABLE_COLUMNNAMES[i]);
		}
		tablemodelGameStat = new DefaultTableModel(strGameStatTableColumnNames, 0);
		tableGameStat = new JTable(tablemodelGameStat);
		tableGameStat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableGameStat.setDefaultEditor(Object.class, null);
		tableGameStat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableGameStat.getTableHeader().setReorderingAllowed(false);
		tableGameStat.setComponentPopupMenu(new TablePopupMenu(tableGameStat));

		TableColumnModel tm2 = tableGameStat.getColumnModel();
		tm2.getColumn(0).setPreferredWidth(propConfig.getProperty("tableGameStat.width.rank", 30));			// Rank
		tm2.getColumn(1).setPreferredWidth(propConfig.getProperty("tableGameStat.width.name", 100));		// Name
		tm2.getColumn(2).setPreferredWidth(propConfig.getProperty("tableGameStat.width.attack", 55));		// Attack count
		tm2.getColumn(3).setPreferredWidth(propConfig.getProperty("tableGameStat.width.apl", 55));			// APL
		tm2.getColumn(4).setPreferredWidth(propConfig.getProperty("tableGameStat.width.apm", 55));			// APM
		tm2.getColumn(5).setPreferredWidth(propConfig.getProperty("tableGameStat.width.lines", 55));		// Line count
		tm2.getColumn(6).setPreferredWidth(propConfig.getProperty("tableGameStat.width.lpm", 55));			// LPM
		tm2.getColumn(7).setPreferredWidth(propConfig.getProperty("tableGameStat.width.piece", 55));		// Piece count
		tm2.getColumn(8).setPreferredWidth(propConfig.getProperty("tableGameStat.width.pps", 55));			// PPS
		tm2.getColumn(9).setPreferredWidth(propConfig.getProperty("tableGameStat.width.time", 65));			// Time
		tm2.getColumn(10).setPreferredWidth(propConfig.getProperty("tableGameStat.width.ko", 40));			// KO
		tm2.getColumn(11).setPreferredWidth(propConfig.getProperty("tableGameStat.width.wins", 55));		// Win
		tm2.getColumn(12).setPreferredWidth(propConfig.getProperty("tableGameStat.width.games", 55));		// Games

		JScrollPane spTableGameStat = new JScrollPane(tableGameStat);
		spTableGameStat.setMinimumSize(new Dimension(0, 0));
		subpanelGameStat.add(spTableGameStat, "GameStatMP");

		// **** Single player game stats table
		strGameStatTableColumnNames1P = new String[STATTABLE1P_COLUMNNAMES.length];
		for(int i = 0; i < strGameStatTableColumnNames1P.length; i++) {
			strGameStatTableColumnNames1P[i] = getUIText(STATTABLE1P_COLUMNNAMES[i]);
		}
		tablemodelGameStat1P = new DefaultTableModel(strGameStatTableColumnNames1P, 0);
		tableGameStat1P = new JTable(tablemodelGameStat1P);
		tableGameStat1P.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableGameStat1P.setDefaultEditor(Object.class, null);
		tableGameStat1P.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableGameStat1P.getTableHeader().setReorderingAllowed(false);
		tableGameStat1P.setComponentPopupMenu(new TablePopupMenu(tableGameStat1P));

		tm2 = tableGameStat1P.getColumnModel();
		tm2.getColumn(0).setPreferredWidth(propConfig.getProperty("tableGameStat1P.width.description", 100));	// Description
		tm2.getColumn(1).setPreferredWidth(propConfig.getProperty("tableGameStat1P.width.value", 100));	// Value

		JScrollPane spTxtpaneGameStat1P = new JScrollPane(tableGameStat1P);
		spTxtpaneGameStat1P.setMinimumSize(new Dimension(0, 0));
		subpanelGameStat.add(spTxtpaneGameStat1P, "GameStat1P");

		// ** Chat panel(Under)
		JPanel subpanelRoomChat = new JPanel(new BorderLayout());
		subpanelRoomChat.setMinimumSize(new Dimension(0,0));
		splitRoom.setBottomComponent(subpanelRoomChat);

		// *** Chat logAndPlayerPartition line of the list(Room screen)
		splitRoomChat = new JSplitPane();
		splitRoomChat.setDividerLocation(propConfig.getProperty("room.splitRoomChat.location", 350));
		subpanelRoomChat.add(splitRoomChat, BorderLayout.CENTER);

		// **** Chat log(Room screen)
		txtpaneRoomChatLog = new JTextPane();
		txtpaneRoomChatLog.setComponentPopupMenu(new LogPopupMenu(txtpaneRoomChatLog));
		txtpaneRoomChatLog.addKeyListener(new LogKeyAdapter());
		JScrollPane spTxtpaneRoomChatLog = new JScrollPane(txtpaneRoomChatLog);
		spTxtpaneRoomChatLog.setMinimumSize(new Dimension(0,0));
		splitRoomChat.setLeftComponent(spTxtpaneRoomChatLog);

		// **** PlayerList(Room screen)
		sameRoomPlayerInfoList = new LinkedList<NetPlayerInfo>();
		listmodelRoomChatPlayerList = new DefaultListModel();
		listboxRoomChatPlayerList = new JList(listmodelRoomChatPlayerList);
		listboxRoomChatPlayerList.setComponentPopupMenu(new ListBoxPopupMenu(listboxRoomChatPlayerList));
		JScrollPane spListboxRoomChatPlayerList = new JScrollPane(listboxRoomChatPlayerList);
		spListboxRoomChatPlayerList.setMinimumSize(new Dimension(0,0));
		splitRoomChat.setRightComponent(spListboxRoomChatPlayerList);

		// *** Chat input Column panel(Room screen)
		JPanel subpanelRoomChatInputArea = new JPanel(new BorderLayout());
		subpanelRoomChat.add(subpanelRoomChatInputArea, BorderLayout.SOUTH);

		// **** Chat input Column(Room screen)
		txtfldRoomChatInput = new JTextField();
		txtfldRoomChatInput.setComponentPopupMenu(new TextComponentPopupMenu(txtfldRoomChatInput));
		subpanelRoomChatInputArea.add(txtfldRoomChatInput, BorderLayout.CENTER);

		// **** Submit chat button(Room screen)
		btnRoomChatSend = new JButton(getUIText("Room_ChatSend"));
		btnRoomChatSend.addActionListener(this);
		btnRoomChatSend.setActionCommand("Room_ChatSend");
		btnRoomChatSend.setMnemonic('S');
		subpanelRoomChatInputArea.add(btnRoomChatSend, BorderLayout.EAST);
	}

	/**
	 * Server-add screen initialization
	 */
	protected void initServerAddUI() {
		// Add Server screen
		JPanel mainpanelServerAdd = new JPanel(new BorderLayout());
		this.getContentPane().add(mainpanelServerAdd, SCREENCARD_NAMES[SCREENCARD_SERVERADD]);

		// * Add Server screen panel(Another panel because it would have been stretched vertically and simply added1I use sheet)
		JPanel containerpanelServerAdd = new JPanel();
		containerpanelServerAdd.setLayout(new BoxLayout(containerpanelServerAdd, BoxLayout.Y_AXIS));
		mainpanelServerAdd.add(containerpanelServerAdd, BorderLayout.NORTH);

		// ** Panel host name
		JPanel subpanelHost = new JPanel(new BorderLayout());
		containerpanelServerAdd.add(subpanelHost);

		// *** Name or &quot;hostIPAddress:&quot;Label
		JLabel labelHost = new JLabel(getUIText("ServerAdd_Host"));
		subpanelHost.add(labelHost, BorderLayout.WEST);

		// *** Host name input Column
		txtfldServerAddHost = new JTextField();
		txtfldServerAddHost.setComponentPopupMenu(new TextComponentPopupMenu(txtfldServerAddHost));
		subpanelHost.add(txtfldServerAddHost, BorderLayout.CENTER);

		// **  buttonPanel type
		JPanel subpanelButtons = new JPanel();
		subpanelButtons.setLayout(new BoxLayout(subpanelButtons, BoxLayout.X_AXIS));
		containerpanelServerAdd.add(subpanelButtons);

		// *** OK button
		btnServerAddOK = new JButton(getUIText("ServerAdd_OK"));
		btnServerAddOK.addActionListener(this);
		btnServerAddOK.setActionCommand("ServerAdd_OK");
		btnServerAddOK.setMnemonic('O');
		btnServerAddOK.setMaximumSize(new Dimension(Short.MAX_VALUE, btnServerAddOK.getMaximumSize().height));
		subpanelButtons.add(btnServerAddOK);

		// *** Cancel button
		JButton btnServerAddCancel = new JButton(getUIText("ServerAdd_Cancel"));
		btnServerAddCancel.addActionListener(this);
		btnServerAddCancel.setActionCommand("ServerAdd_Cancel");
		btnServerAddCancel.setMnemonic('C');
		btnServerAddCancel.setMaximumSize(new Dimension(Short.MAX_VALUE, btnServerAddCancel.getMaximumSize().height));
		subpanelButtons.add(btnServerAddCancel);
	}

	/**
	 * Create rated screen card while waiting for presets to arrive from server
	 */
	protected void initCreateRatedWaitingUI() {
		// Main panel
		JPanel mainpanelCreateRatedWaiting = new JPanel(new BorderLayout());
		this.getContentPane().add(mainpanelCreateRatedWaiting, SCREENCARD_NAMES[SCREENCARD_CREATERATED_WAITING]);

		// * Container panel
		JPanel containerpanelCreateRatedWaiting = new JPanel();
		containerpanelCreateRatedWaiting.setLayout(new BoxLayout(containerpanelCreateRatedWaiting, BoxLayout.Y_AXIS));
		mainpanelCreateRatedWaiting.add(containerpanelCreateRatedWaiting, BorderLayout.NORTH);

		// ** Subpanel for label
		JPanel subpanelText = new JPanel(new BorderLayout());
		containerpanelCreateRatedWaiting.add(subpanelText, BorderLayout.CENTER);

		// *** "Please wait while preset information is retrieved from the server" label
		JLabel labelWaiting = new JLabel(getUIText("CreateRated_Waiting_Text"));
		subpanelText.add(labelWaiting,BorderLayout.CENTER);

		// ** Subpanel for cancel button
		JPanel subpanelButtons = new JPanel();
		mainpanelCreateRatedWaiting.add(subpanelButtons, BorderLayout.SOUTH);

		// *** Cancel Button
		btnCreateRatedWaitingCancel = new JButton(getUIText("CreateRated_Waiting_Cancel"));
		btnCreateRatedWaitingCancel.addActionListener(this);
		btnCreateRatedWaitingCancel.setActionCommand("CreateRated_Waiting_Cancel");
		btnCreateRatedWaitingCancel.setMnemonic('C');
		btnCreateRatedWaitingCancel.setMaximumSize(new Dimension(Short.MAX_VALUE,
				btnCreateRatedWaitingCancel.getMaximumSize().height));
		subpanelButtons.add(btnCreateRatedWaitingCancel, BorderLayout.SOUTH);
	}

	protected void initCreateRatedUI() {
		// Main panel
		JPanel mainpanelCreateRated = new JPanel(new BorderLayout());
		this.getContentPane().add(mainpanelCreateRated, SCREENCARD_NAMES[SCREENCARD_CREATERATED_WAITING]);

		// * Container panel
		JPanel containerpanelCreateRated = new JPanel();
		containerpanelCreateRated.setLayout(new BoxLayout(containerpanelCreateRated, BoxLayout.Y_AXIS));
		mainpanelCreateRated.add(containerpanelCreateRated, BorderLayout.NORTH);

		// ** Subpanel for preset selection
		JPanel subpanelName = new JPanel(new BorderLayout());
		containerpanelCreateRated.add(subpanelName);

		// *** "Room Name:" label
		JLabel labelName = new JLabel(getUIText("CreateRated_Name"));
		subpanelName.add(labelName, BorderLayout.WEST);

		// *** Room name textfield
		txtfldCreateRatedName = new JTextField();
		txtfldCreateRatedName.setComponentPopupMenu(new TextComponentPopupMenu(txtfldCreateRatedName));
		txtfldCreateRatedName.setToolTipText(getUIText("CreateRated_Name_Tip"));
		subpanelName.add(txtfldCreateRatedName, BorderLayout.CENTER);

		// ** Subpanel for preset selection
		JPanel subpanelPresetSelect = new JPanel(new BorderLayout());
		containerpanelCreateRated.add(subpanelPresetSelect);

		// *** "Preset:" label
		JLabel labelWaiting = new JLabel(getUIText("CreateRated_Preset"));
		subpanelPresetSelect.add(labelWaiting, BorderLayout.WEST);

		// *** Presets
		comboboxCreateRatedPresets = new JComboBox(new String[] {"Select..."});
		comboboxCreateRatedPresets.setSelectedIndex(propConfig.getProperty("createrated.defaultPreset", 0));
		comboboxCreateRatedPresets.setPreferredSize(new Dimension(200, 20));
		comboboxCreateRatedPresets.setToolTipText(getUIText("CreateRated_Preset_Tip"));
		subpanelPresetSelect.add(comboboxCreateRatedPresets, BorderLayout.EAST);

		// ** Number of players panel
		JPanel subpanelMaxPlayers = new JPanel(new BorderLayout());
		containerpanelCreateRated.add(subpanelMaxPlayers);

		// *** Number of players label
		JLabel labelMaxPlayers = new JLabel(getUIText("CreateRated_MaxPlayers"));
		subpanelMaxPlayers.add(labelMaxPlayers, BorderLayout.WEST);

		// *** Number of players textfield
		int defaultMaxPlayers = propConfig.getProperty("createrated.defaultMaxPlayers", 6);
		spinnerCreateRatedMaxPlayers = new JSpinner(new SpinnerNumberModel(defaultMaxPlayers, 2, 6, 1));
		spinnerCreateRatedMaxPlayers.setPreferredSize(new Dimension(200, 20));
		spinnerCreateRatedMaxPlayers.setToolTipText(getUIText("CreateRated_MaxPlayers_Tip"));
		subpanelMaxPlayers.add(spinnerCreateRatedMaxPlayers, BorderLayout.EAST);

		// ** Subpanel for buttons
		JPanel subpanelButtons = new JPanel();
		mainpanelCreateRated.add(subpanelButtons, BorderLayout.SOUTH);

		// *** OK button
		btnCreateRatedOK = new JButton(getUIText("CreateRated_OK"));
		btnCreateRatedOK.addActionListener(this);
		btnCreateRatedOK.setActionCommand("CreateRated_OK");
		btnCreateRatedOK.setMnemonic('O');
		btnCreateRatedOK.setMaximumSize(new Dimension(Short.MAX_VALUE, btnCreateRatedOK.getMaximumSize().height));
		subpanelButtons.add(btnCreateRatedOK);

		// *** Custom button
		btnCreateRatedCustom = new JButton(getUIText("CreateRated_Custom"));
		btnCreateRatedCustom.addActionListener(this);
		btnCreateRatedCustom.setActionCommand("CreateRated_Custom");
		btnCreateRatedCustom.setMnemonic('U');
		btnCreateRatedCustom.setMaximumSize(new Dimension(Short.MAX_VALUE, btnCreateRatedCustom.getMaximumSize().height));
		subpanelButtons.add(btnCreateRatedCustom);

		// *** Cancel Button
		btnCreateRatedCancel = new JButton(getUIText("CreateRated_Cancel"));
		btnCreateRatedCancel.addActionListener(this);
		btnCreateRatedCancel.setActionCommand("CreateRated_Cancel");
		btnCreateRatedCancel.setMnemonic('C');
		btnCreateRatedCancel.setMaximumSize(new Dimension(Short.MAX_VALUE, btnCreateRatedCancel.getMaximumSize().height));
		subpanelButtons.add(btnCreateRatedCancel);
	}

	/**
	 * Create room scren initialization
	 */
	protected void initCreateRoomUI() {
		// Create Screen Room
		JPanel mainpanelCreateRoom = new JPanel(new BorderLayout());
		this.getContentPane().add(mainpanelCreateRoom, SCREENCARD_NAMES[SCREENCARD_CREATEROOM]);

		// Tab
		JTabbedPane tabbedPane = new JTabbedPane();
		mainpanelCreateRoom.add(tabbedPane, BorderLayout.CENTER);

		// tabs

		// * Basic Settings panel
		JPanel containerpanelCreateRoomMainOwner = new JPanel(new BorderLayout());
		tabbedPane.addTab(getUIText("CreateRoom_Tab_Main"), containerpanelCreateRoomMainOwner);

		// * Speed ​​setting panel(Stretching for prevention)
		JPanel containerpanelCreateRoomSpeedOwner = new JPanel(new BorderLayout());
		tabbedPane.addTab(getUIText("CreateRoom_Tab_Speed"), containerpanelCreateRoomSpeedOwner);

		// * Bonus tab
		JPanel containerpanelCreateRoomBonusOwner = new JPanel(new BorderLayout());
		tabbedPane.addTab(getUIText("CreateRoom_Tab_Bonus"), containerpanelCreateRoomBonusOwner);

		// * Garbage tab
		JPanel containerpanelCreateRoomGarbageOwner = new JPanel(new BorderLayout());
		tabbedPane.addTab(getUIText("CreateRoom_Tab_Garbage"), containerpanelCreateRoomGarbageOwner);

		// * Miscellaneous tab
		JPanel containerpanelCreateRoomMiscOwner = new JPanel(new BorderLayout());
		tabbedPane.addTab(getUIText("CreateRoom_Tab_Misc"), containerpanelCreateRoomMiscOwner);

		// * Preset tab
		JPanel containerpanelCreateRoomPresetOwner = new JPanel(new BorderLayout());
		tabbedPane.addTab(getUIText("CreateRoom_Tab_Preset"), containerpanelCreateRoomPresetOwner);

		// general tab

		// * Speed ​​setting panel(Body)
		JPanel containerpanelCreateRoomMain = new JPanel();
		containerpanelCreateRoomMain.setLayout(new BoxLayout(containerpanelCreateRoomMain, BoxLayout.Y_AXIS));
		containerpanelCreateRoomMainOwner.add(containerpanelCreateRoomMain, BorderLayout.NORTH);

		// ** Panel Room name
		JPanel subpanelName = new JPanel(new BorderLayout());
		containerpanelCreateRoomMain.add(subpanelName);

		// *** Name &quot;Room:&quot;Label
		JLabel labelName = new JLabel(getUIText("CreateRoom_Name"));
		subpanelName.add(labelName, BorderLayout.WEST);

		// *** Room name input Column
		txtfldCreateRoomName = new JTextField();
		txtfldCreateRoomName.setComponentPopupMenu(new TextComponentPopupMenu(txtfldCreateRoomName));
		txtfldCreateRoomName.setToolTipText(getUIText("CreateRoom_Name_Tip"));
		subpanelName.add(txtfldCreateRoomName, BorderLayout.CENTER);

		// ** Game Mode panel
		JPanel subpanelMode = new JPanel(new BorderLayout());
		containerpanelCreateRoomMain.add(subpanelMode);

		// *** Mode label
		JLabel labelMode = new JLabel(getUIText("CreateRoom_Mode"));
		subpanelMode.add(labelMode, BorderLayout.WEST);

		// *** Mode Combobox
		DefaultComboBoxModel modelMode = new DefaultComboBoxModel();
		loadModeList(modelMode, "config/list/netlobby_multimode.lst");
		comboboxCreateRoomMode = new JComboBox(modelMode);
		comboboxCreateRoomMode.setPreferredSize(new Dimension(200, 20));
		comboboxCreateRoomMode.setToolTipText(getUIText("CreateRoom_Mode_Tip"));
		subpanelMode.add(comboboxCreateRoomMode, BorderLayout.EAST);

		// ** People participatecountPanel
		JPanel subpanelMaxPlayers = new JPanel(new BorderLayout());
		containerpanelCreateRoomMain.add(subpanelMaxPlayers);

		// *** Human participation &quot;count:&quot;Label
		JLabel labelMaxPlayers = new JLabel(getUIText("CreateRoom_MaxPlayers"));
		subpanelMaxPlayers.add(labelMaxPlayers, BorderLayout.WEST);

		// *** People participatecountSelection
		int defaultMaxPlayers = propConfig.getProperty("createroom.defaultMaxPlayers", 6);
		spinnerCreateRoomMaxPlayers = new JSpinner(new SpinnerNumberModel(defaultMaxPlayers, 2, 6, 1));
		spinnerCreateRoomMaxPlayers.setPreferredSize(new Dimension(200, 20));
		spinnerCreateRoomMaxPlayers.setToolTipText(getUIText("CreateRoom_MaxPlayers_Tip"));
		subpanelMaxPlayers.add(spinnerCreateRoomMaxPlayers, BorderLayout.EAST);

		// ** HurryupSecondcountPanel
		JPanel subpanelHurryupSeconds = new JPanel(new BorderLayout());
		containerpanelCreateRoomMain.add(subpanelHurryupSeconds);

		// *** &#39;HURRY UPSeconds before the startcount:&quot;Label
		JLabel labelHurryupSeconds = new JLabel(getUIText("CreateRoom_HurryupSeconds"));
		subpanelHurryupSeconds.add(labelHurryupSeconds, BorderLayout.WEST);

		// *** HurryupSecondcount
		int defaultHurryupSeconds = propConfig.getProperty("createroom.defaultHurryupSeconds", 180);
		spinnerCreateRoomHurryupSeconds = new JSpinner(new SpinnerNumberModel(defaultHurryupSeconds, -1, 999, 1));
		spinnerCreateRoomHurryupSeconds.setPreferredSize(new Dimension(200, 20));
		spinnerCreateRoomHurryupSeconds.setToolTipText(getUIText("CreateRoom_HurryupSeconds_Tip"));
		subpanelHurryupSeconds.add(spinnerCreateRoomHurryupSeconds, BorderLayout.EAST);

		// ** HurryupPanel spacing
		JPanel subpanelHurryupInterval = new JPanel(new BorderLayout());
		containerpanelCreateRoomMain.add(subpanelHurryupInterval);

		// *** &#39;HURRY UPLater, Interval overcall the floor:&quot;Label
		JLabel labelHurryupInterval = new JLabel(getUIText("CreateRoom_HurryupInterval"));
		subpanelHurryupInterval.add(labelHurryupInterval, BorderLayout.WEST);

		// *** HurryupInterval
		int defaultHurryupInterval = propConfig.getProperty("createroom.defaultHurryupInterval", 5);
		spinnerCreateRoomHurryupInterval = new JSpinner(new SpinnerNumberModel(defaultHurryupInterval, 1, 99, 1));
		spinnerCreateRoomHurryupInterval.setPreferredSize(new Dimension(200, 20));
		spinnerCreateRoomHurryupInterval.setToolTipText(getUIText("CreateRoom_HurryupInterval_Tip"));
		subpanelHurryupInterval.add(spinnerCreateRoomHurryupInterval, BorderLayout.EAST);

		// ** MapSetIDPanel
		JPanel subpanelMapSetID = new JPanel(new BorderLayout());
		containerpanelCreateRoomMain.add(subpanelMapSetID);

		// *** &#39;MapSetID:&quot;Label
		JLabel labelMapSetID = new JLabel(getUIText("CreateRoom_MapSetID"));
		subpanelMapSetID.add(labelMapSetID, BorderLayout.WEST);

		// *** MapSetID
		int defaultMapSetID = propConfig.getProperty("createroom.defaultMapSetID", 0);
		spinnerCreateRoomMapSetID = new JSpinner(new SpinnerNumberModel(defaultMapSetID, 0, 99, 1));
		spinnerCreateRoomMapSetID.setPreferredSize(new Dimension(200, 20));
		spinnerCreateRoomMapSetID.setToolTipText(getUIText("CreateRoom_MapSetID_Tip"));
		subpanelMapSetID.add(spinnerCreateRoomMapSetID, BorderLayout.EAST);

		// ** Map is enabled
		chkboxCreateRoomUseMap = new JCheckBox(getUIText("CreateRoom_UseMap"));
		chkboxCreateRoomUseMap.setMnemonic('P');
		chkboxCreateRoomUseMap.setSelected(propConfig.getProperty("createroom.defaultUseMap", false));
		chkboxCreateRoomUseMap.setToolTipText(getUIText("CreateRoom_UseMap_Tip"));
		containerpanelCreateRoomMain.add(chkboxCreateRoomUseMap);

		// ** Of all fixed rules
		chkboxCreateRoomRuleLock = new JCheckBox(getUIText("CreateRoom_RuleLock"));
		chkboxCreateRoomRuleLock.setMnemonic('L');
		chkboxCreateRoomRuleLock.setSelected(propConfig.getProperty("createroom.defaultRuleLock", false));
		chkboxCreateRoomRuleLock.setToolTipText(getUIText("CreateRoom_RuleLock_Tip"));
		containerpanelCreateRoomMain.add(chkboxCreateRoomRuleLock);

		// speed tab

		// * Speed ​​setting panel(Body)
		JPanel containerpanelCreateRoomSpeed = new JPanel();
		containerpanelCreateRoomSpeed.setLayout(new BoxLayout(containerpanelCreateRoomSpeed, BoxLayout.Y_AXIS));
		containerpanelCreateRoomSpeedOwner.add(containerpanelCreateRoomSpeed, BorderLayout.NORTH);

		// ** Fall velocity(Molecule)Panel
		JPanel subpanelGravity = new JPanel(new BorderLayout());
		containerpanelCreateRoomSpeed.add(subpanelGravity);

		// *** Fall velocity &quot;(Molecule):&quot;Label
		JLabel labelGravity = new JLabel(getUIText("CreateRoom_Gravity"));
		subpanelGravity.add(labelGravity, BorderLayout.WEST);

		// *** Fall velocity(Molecule)
		int defaultGravity = propConfig.getProperty("createroom.defaultGravity", 1);
		spinnerCreateRoomGravity = new JSpinner(new SpinnerNumberModel(defaultGravity, -1, 99999, 1));
		spinnerCreateRoomGravity.setPreferredSize(new Dimension(200, 20));
		subpanelGravity.add(spinnerCreateRoomGravity, BorderLayout.EAST);

		// ** Fall velocity(Denominator)Panel
		JPanel subpanelDenominator = new JPanel(new BorderLayout());
		containerpanelCreateRoomSpeed.add(subpanelDenominator);

		// *** Fall velocity &quot;(Denominator):&quot;Label
		JLabel labelDenominator = new JLabel(getUIText("CreateRoom_Denominator"));
		subpanelDenominator.add(labelDenominator, BorderLayout.WEST);

		// *** Fall velocity(Denominator)
		int defaultDenominator = propConfig.getProperty("createroom.defaultDenominator", 60);
		spinnerCreateRoomDenominator = new JSpinner(new SpinnerNumberModel(defaultDenominator, 0, 99999, 1));
		spinnerCreateRoomDenominator.setPreferredSize(new Dimension(200, 20));
		subpanelDenominator.add(spinnerCreateRoomDenominator, BorderLayout.EAST);

		// ** AREPanel
		JPanel subpanelARE = new JPanel(new BorderLayout());
		containerpanelCreateRoomSpeed.add(subpanelARE);

		// *** &#39;ARE:&quot;Label
		JLabel labelARE = new JLabel(getUIText("CreateRoom_ARE"));
		subpanelARE.add(labelARE, BorderLayout.WEST);

		// *** ARE
		int defaultARE = propConfig.getProperty("createroom.defaultARE", 0);
		spinnerCreateRoomARE = new JSpinner(new SpinnerNumberModel(defaultARE, 0, 99, 1));
		spinnerCreateRoomARE.setPreferredSize(new Dimension(200, 20));
		subpanelARE.add(spinnerCreateRoomARE, BorderLayout.EAST);

		// ** ARE after line clearPanel
		JPanel subpanelARELine = new JPanel(new BorderLayout());
		containerpanelCreateRoomSpeed.add(subpanelARELine);

		// *** &#39;ARE after line clear:&quot;Label
		JLabel labelARELine = new JLabel(getUIText("CreateRoom_ARELine"));
		subpanelARELine.add(labelARELine, BorderLayout.WEST);

		// *** ARE after line clear
		int defaultARELine = propConfig.getProperty("createroom.defaultARELine", 0);
		spinnerCreateRoomARELine = new JSpinner(new SpinnerNumberModel(defaultARELine, 0, 99, 1));
		spinnerCreateRoomARELine.setPreferredSize(new Dimension(200, 20));
		subpanelARELine.add(spinnerCreateRoomARELine, BorderLayout.EAST);

		// ** Line clear timePanel
		JPanel subpanelLineDelay = new JPanel(new BorderLayout());
		containerpanelCreateRoomSpeed.add(subpanelLineDelay);

		// *** &#39;Line clear time:&quot;Label
		JLabel labelLineDelay = new JLabel(getUIText("CreateRoom_LineDelay"));
		subpanelLineDelay.add(labelLineDelay, BorderLayout.WEST);

		// *** Line clear time
		int defaultLineDelay = propConfig.getProperty("createroom.defaultLineDelay", 0);
		spinnerCreateRoomLineDelay = new JSpinner(new SpinnerNumberModel(defaultLineDelay, 0, 99, 1));
		spinnerCreateRoomLineDelay.setPreferredSize(new Dimension(200, 20));
		subpanelLineDelay.add(spinnerCreateRoomLineDelay, BorderLayout.EAST);

		// ** Fixation timePanel
		JPanel subpanelLockDelay = new JPanel(new BorderLayout());
		containerpanelCreateRoomSpeed.add(subpanelLockDelay);

		// *** &quot;Fixed time:&quot;Label
		JLabel labelLockDelay = new JLabel(getUIText("CreateRoom_LockDelay"));
		subpanelLockDelay.add(labelLockDelay, BorderLayout.WEST);

		// *** Fixation time
		int defaultLockDelay = propConfig.getProperty("createroom.defaultLockDelay", 30);
		spinnerCreateRoomLockDelay = new JSpinner(new SpinnerNumberModel(defaultLockDelay, 0, 98, 1));
		spinnerCreateRoomLockDelay.setPreferredSize(new Dimension(200, 20));
		subpanelLockDelay.add(spinnerCreateRoomLockDelay, BorderLayout.EAST);

		// ** Panel horizontal reservoir
		JPanel subpanelDAS = new JPanel(new BorderLayout());
		containerpanelCreateRoomSpeed.add(subpanelDAS);

		// *** Horizontal reservoir &quot;:&quot;Label
		JLabel labelDAS = new JLabel(getUIText("CreateRoom_DAS"));
		subpanelDAS.add(labelDAS, BorderLayout.WEST);

		// *** Horizontal reservoir
		int defaultDAS = propConfig.getProperty("createroom.defaultDAS", 11);
		spinnerCreateRoomDAS = new JSpinner(new SpinnerNumberModel(defaultDAS, 0, 99, 1));
		spinnerCreateRoomDAS.setPreferredSize(new Dimension(200, 20));
		subpanelDAS.add(spinnerCreateRoomDAS, BorderLayout.EAST);

		// bonus tab

		// bonus panel
		JPanel containerpanelCreateRoomBonus = new JPanel();
		containerpanelCreateRoomBonus.setLayout(new BoxLayout(containerpanelCreateRoomBonus, BoxLayout.Y_AXIS));
		containerpanelCreateRoomBonusOwner.add(containerpanelCreateRoomBonus, BorderLayout.NORTH);

		// ** Spin bonusPanel
		JPanel subpanelTSpinEnableType = new JPanel(new BorderLayout());
		containerpanelCreateRoomBonus.add(subpanelTSpinEnableType);

		// *** &quot;Spin bonus:&quot;Label
		JLabel labelTSpinEnableType = new JLabel(getUIText("CreateRoom_TSpinEnableType"));
		subpanelTSpinEnableType.add(labelTSpinEnableType, BorderLayout.WEST);

		// *** Spin bonus
		String[] strSpinBonusNames = new String[COMBOBOX_SPINBONUS_NAMES.length];
		for(int i = 0; i < strSpinBonusNames.length; i++) {
			strSpinBonusNames[i] = getUIText(COMBOBOX_SPINBONUS_NAMES[i]);
		}
		comboboxCreateRoomTSpinEnableType = new JComboBox(strSpinBonusNames);
		comboboxCreateRoomTSpinEnableType.setSelectedIndex(propConfig.getProperty("createroom.defaultTSpinEnableType", 1));
		comboboxCreateRoomTSpinEnableType.setPreferredSize(new Dimension(200, 20));
		comboboxCreateRoomTSpinEnableType.setToolTipText(getUIText("CreateRoom_TSpinEnableType_Tip"));
		subpanelTSpinEnableType.add(comboboxCreateRoomTSpinEnableType, BorderLayout.EAST);

		// ** Spin check type panel
		JPanel subpanelSpinCheckType = new JPanel(new BorderLayout());
		containerpanelCreateRoomBonus.add(subpanelSpinCheckType);

		// *** Spin check type label
		JLabel labelSpinCheckType = new JLabel(getUIText("CreateRoom_SpinCheckType"));
		subpanelSpinCheckType.add(labelSpinCheckType, BorderLayout.WEST);

		// *** Spin check type combobox
		String[] strSpinCheckTypeNames = new String[COMBOBOX_SPINCHECKTYPE_NAMES.length];
		for(int i = 0; i < strSpinCheckTypeNames.length; i++) {
			strSpinCheckTypeNames[i] = getUIText(COMBOBOX_SPINCHECKTYPE_NAMES[i]);
		}
		comboboxCreateRoomSpinCheckType = new JComboBox(strSpinCheckTypeNames);
		comboboxCreateRoomSpinCheckType.setSelectedIndex(propConfig.getProperty("createroom.defaultSpinCheckType", 0));
		comboboxCreateRoomSpinCheckType.setPreferredSize(new Dimension(200, 20));
		comboboxCreateRoomSpinCheckType.setToolTipText(getUIText("CreateRoom_SpinCheckType_Tip"));
		subpanelSpinCheckType.add(comboboxCreateRoomSpinCheckType, BorderLayout.EAST);

		// ** EZ Spin checkbox
		chkboxCreateRoomTSpinEnableEZ = new JCheckBox(getUIText("CreateRoom_TSpinEnableEZ"));
		chkboxCreateRoomTSpinEnableEZ.setMnemonic('E');
		chkboxCreateRoomTSpinEnableEZ.setSelected(propConfig.getProperty("createroom.defaultTSpinEnableEZ", false));
		chkboxCreateRoomTSpinEnableEZ.setToolTipText(getUIText("CreateRoom_TSpinEnableEZ_Tip"));
		containerpanelCreateRoomBonus.add(chkboxCreateRoomTSpinEnableEZ);

		// ** Flag for enabling B2B
		chkboxCreateRoomB2B = new JCheckBox(getUIText("CreateRoom_B2B"));
		chkboxCreateRoomB2B.setMnemonic('B');
		chkboxCreateRoomB2B.setSelected(propConfig.getProperty("createroom.defaultB2B", true));
		chkboxCreateRoomB2B.setToolTipText(getUIText("CreateRoom_B2B_Tip"));
		containerpanelCreateRoomBonus.add(chkboxCreateRoomB2B);

		// ** Flag for enabling combos
		chkboxCreateRoomCombo = new JCheckBox(getUIText("CreateRoom_Combo"));
		chkboxCreateRoomCombo.setMnemonic('M');
		chkboxCreateRoomCombo.setSelected(propConfig.getProperty("createroom.defaultCombo", true));
		chkboxCreateRoomCombo.setToolTipText(getUIText("CreateRoom_Combo_Tip"));
		containerpanelCreateRoomBonus.add(chkboxCreateRoomCombo);

		// ** Bravo bonus
		chkboxCreateRoomBravo = new JCheckBox(getUIText("CreateRoom_Bravo"));
		chkboxCreateRoomBravo.setMnemonic('A');
		chkboxCreateRoomBravo.setSelected(propConfig.getProperty("createroom.defaultBravo", true));
		chkboxCreateRoomBravo.setToolTipText(getUIText("CreateRoom_Bravo_Tip"));
		containerpanelCreateRoomBonus.add(chkboxCreateRoomBravo);

		// garbage tab

		// garbage panel
		JPanel containerpanelCreateRoomGarbage = new JPanel();
		containerpanelCreateRoomGarbage.setLayout(new BoxLayout(containerpanelCreateRoomGarbage, BoxLayout.Y_AXIS));
		containerpanelCreateRoomGarbageOwner.add(containerpanelCreateRoomGarbage, BorderLayout.NORTH);

		// ** Garbage change rate panel
		JPanel subpanelGarbagePercent = new JPanel(new BorderLayout());
		containerpanelCreateRoomGarbage.add(subpanelGarbagePercent);

		// ** Label for garbage change rate
		JLabel labelGarbagePercent = new JLabel(getUIText("CreateRoom_GarbagePercent"));
		subpanelGarbagePercent.add(labelGarbagePercent, BorderLayout.WEST);

		// ** Spinner for garbage change rate
		int defaultGarbagePercent = propConfig.getProperty("createroom.defaultGarbagePercent", 90);
		spinnerCreateRoomGarbagePercent = new JSpinner(new SpinnerNumberModel(defaultGarbagePercent, 0, 100, 10));
		spinnerCreateRoomGarbagePercent.setPreferredSize(new Dimension(200, 20));
		spinnerCreateRoomGarbagePercent.setToolTipText(getUIText("CreateRoom_GarbagePercent_Tip"));
		subpanelGarbagePercent.add(spinnerCreateRoomGarbagePercent, BorderLayout.EAST);

		// ** Target timer panel
		JPanel subpanelTargetTimer = new JPanel(new BorderLayout());
		containerpanelCreateRoomGarbage.add(subpanelTargetTimer);

		// ** Label for target timer
		JLabel labelTargetTimer = new JLabel(getUIText("CreateRoom_TargetTimer"));
		subpanelTargetTimer.add(labelTargetTimer, BorderLayout.WEST);

		// ** Spinner for target timer
		int defaultTargetTimer = propConfig.getProperty("createroom.defaultTargetTimer", 60);
		spinnerCreateRoomTargetTimer = new JSpinner(new SpinnerNumberModel(defaultTargetTimer, 0, 3600, 1));
		spinnerCreateRoomTargetTimer.setPreferredSize(new Dimension(200, 20));
		spinnerCreateRoomTargetTimer.setToolTipText(getUIText("CreateRoom_TargetTimer_Tip"));
		subpanelTargetTimer.add(spinnerCreateRoomTargetTimer, BorderLayout.EAST);

		// ** Set garbage type
		chkboxCreateRoomGarbageChangePerAttack = new JCheckBox(getUIText("CreateRoom_GarbageChangePerAttack"));
		chkboxCreateRoomGarbageChangePerAttack.setMnemonic('G');
		chkboxCreateRoomGarbageChangePerAttack.setSelected(propConfig.getProperty("createroom.defaultGarbageChangePerAttack", true));
		chkboxCreateRoomGarbageChangePerAttack.setToolTipText(getUIText("CreateRoom_GarbageChangePerAttack_Tip"));
		containerpanelCreateRoomGarbage.add(chkboxCreateRoomGarbageChangePerAttack);

		// ** Divide change rate by live players/teams
		chkboxCreateRoomDivideChangeRateByPlayers = new JCheckBox(getUIText("CreateRoom_DivideChangeRateByPlayers"));
		chkboxCreateRoomDivideChangeRateByPlayers.setSelected(propConfig.getProperty("createroom.defaultDivideChangeRateByPlayers", false));
		chkboxCreateRoomDivideChangeRateByPlayers.setToolTipText(getUIText("CreateRoom_DivideChangeRateByPlayers_Tip"));
		containerpanelCreateRoomGarbage.add(chkboxCreateRoomDivideChangeRateByPlayers);

		// ** B2B chunk
		chkboxCreateRoomB2BChunk = new JCheckBox(getUIText("CreateRoom_B2BChunk"));
		chkboxCreateRoomB2BChunk.setMnemonic('B');
		chkboxCreateRoomB2BChunk.setSelected(propConfig.getProperty("createroom.defaultB2BChunk", false));
		chkboxCreateRoomB2BChunk.setToolTipText(getUIText("CreateRoom_B2BChunk_Tip"));
		containerpanelCreateRoomGarbage.add(chkboxCreateRoomB2BChunk);

		// ** Rensa/Combo Block
		chkboxCreateRoomRensaBlock = new JCheckBox(getUIText("CreateRoom_RensaBlock"));
		chkboxCreateRoomRensaBlock.setMnemonic('E');
		chkboxCreateRoomRensaBlock.setSelected(propConfig.getProperty("createroom.defaultRensaBlock", true));
		chkboxCreateRoomRensaBlock.setToolTipText(getUIText("CreateRoom_RensaBlock_Tip"));
		containerpanelCreateRoomGarbage.add(chkboxCreateRoomRensaBlock);

		// ** Garbage countering
		chkboxCreateRoomCounter = new JCheckBox(getUIText("CreateRoom_Counter"));
		chkboxCreateRoomCounter.setMnemonic('C');
		chkboxCreateRoomCounter.setSelected(propConfig.getProperty("createroom.defaultCounter", true));
		chkboxCreateRoomCounter.setToolTipText(getUIText("CreateRoom_Counter_Tip"));
		containerpanelCreateRoomGarbage.add(chkboxCreateRoomCounter);

		// ** 3If I live more than Attack Reduce the force
		chkboxCreateRoomReduceLineSend = new JCheckBox(getUIText("CreateRoom_ReduceLineSend"));
		chkboxCreateRoomReduceLineSend.setMnemonic('R');
		chkboxCreateRoomReduceLineSend.setSelected(propConfig.getProperty("createroom.defaultReduceLineSend", true));
		chkboxCreateRoomReduceLineSend.setToolTipText(getUIText("CreateRoom_ReduceLineSend_Tip"));
		containerpanelCreateRoomGarbage.add(chkboxCreateRoomReduceLineSend);

		// ** Fragmentarygarbage blockUsing the system
		chkboxCreateRoomUseFractionalGarbage = new JCheckBox(getUIText("CreateRoom_UseFractionalGarbage"));
		chkboxCreateRoomUseFractionalGarbage.setMnemonic('F');
		chkboxCreateRoomUseFractionalGarbage.setSelected(propConfig.getProperty("createroom.defaultUseFractionalGarbage", false));
		chkboxCreateRoomUseFractionalGarbage.setToolTipText(getUIText("CreateRoom_UseFractionalGarbage_Tip"));
		containerpanelCreateRoomGarbage.add(chkboxCreateRoomUseFractionalGarbage);

		// *** Use target system
		chkboxCreateRoomIsTarget = new JCheckBox(getUIText("CreateRoom_IsTarget"));
		chkboxCreateRoomIsTarget.setMnemonic('T');
		chkboxCreateRoomIsTarget.setSelected(propConfig.getProperty("createroom.defaultIsTarget", false));
		chkboxCreateRoomIsTarget.setToolTipText(getUIText("CreateRoom_IsTarget_Tip"));
		containerpanelCreateRoomGarbage.add(chkboxCreateRoomIsTarget);

		// misc tab

		// misc panel
		JPanel containerpanelCreateRoomMisc = new JPanel();
		containerpanelCreateRoomMisc.setLayout(new BoxLayout(containerpanelCreateRoomMisc, BoxLayout.Y_AXIS));
		containerpanelCreateRoomMiscOwner.add(containerpanelCreateRoomMisc, BorderLayout.NORTH);

		// ** To wait before auto-start timePanel
		JPanel subpanelAutoStartSeconds = new JPanel(new BorderLayout());
		containerpanelCreateRoomMisc.add(subpanelAutoStartSeconds);

		// *** To wait before auto-start &quot; time:&quot;Label
		JLabel labelAutoStartSeconds = new JLabel(getUIText("CreateRoom_AutoStartSeconds"));
		subpanelAutoStartSeconds.add(labelAutoStartSeconds, BorderLayout.WEST);

		// *** To wait before auto-start time
		int defaultAutoStartSeconds = propConfig.getProperty("createroom.defaultAutoStartSeconds", 15);
		spinnerCreateRoomAutoStartSeconds = new JSpinner(new SpinnerNumberModel(defaultAutoStartSeconds, 0, 999, 1));
		spinnerCreateRoomAutoStartSeconds.setPreferredSize(new Dimension(200, 20));
		spinnerCreateRoomAutoStartSeconds.setToolTipText(getUIText("CreateRoom_AutoStartSeconds_Tip"));
		subpanelAutoStartSeconds.add(spinnerCreateRoomAutoStartSeconds, BorderLayout.EAST);

		// ** TNET2TypeAutomatically start timerI use
		chkboxCreateRoomAutoStartTNET2 = new JCheckBox(getUIText("CreateRoom_AutoStartTNET2"));
		chkboxCreateRoomAutoStartTNET2.setMnemonic('A');
		chkboxCreateRoomAutoStartTNET2.setSelected(propConfig.getProperty("createroom.defaultAutoStartTNET2", false));
		chkboxCreateRoomAutoStartTNET2.setToolTipText(getUIText("CreateRoom_AutoStartTNET2_Tip"));
		containerpanelCreateRoomMisc.add(chkboxCreateRoomAutoStartTNET2);

		// ** SomeoneCancelWasTimerInvalidation
		chkboxCreateRoomDisableTimerAfterSomeoneCancelled = new JCheckBox(getUIText("CreateRoom_DisableTimerAfterSomeoneCancelled"));
		chkboxCreateRoomDisableTimerAfterSomeoneCancelled.setMnemonic('D');
		chkboxCreateRoomDisableTimerAfterSomeoneCancelled.setSelected(propConfig.getProperty("createroom.defaultDisableTimerAfterSomeoneCancelled", false));
		chkboxCreateRoomDisableTimerAfterSomeoneCancelled.setToolTipText(getUIText("CreateRoom_DisableTimerAfterSomeoneCancelled_Tip"));
		containerpanelCreateRoomMisc.add(chkboxCreateRoomDisableTimerAfterSomeoneCancelled);

		// Preset tab

		// * Preset panel
		JPanel containerpanelCreateRoomPreset = new JPanel();
		containerpanelCreateRoomPreset.setLayout(new BoxLayout(containerpanelCreateRoomPreset, BoxLayout.Y_AXIS));
		containerpanelCreateRoomPresetOwner.add(containerpanelCreateRoomPreset, BorderLayout.NORTH);

		// ** Preset number panel
		JPanel subpanelPresetID = new JPanel(new BorderLayout());
		subpanelPresetID.setAlignmentX(0f);
		containerpanelCreateRoomPreset.add(subpanelPresetID);

		// *** "Preset number:" Label
		JLabel labelPresetID = new JLabel(getUIText("CreateRoom_PresetID"));
		subpanelPresetID.add(labelPresetID, BorderLayout.WEST);

		// *** Preset number selector
		int defaultPresetID = propConfig.getProperty("createroom.defaultPresetID", 0);
		spinnerCreateRoomPresetID = new JSpinner(new SpinnerNumberModel(defaultPresetID, 0, 999, 1));
		spinnerCreateRoomPresetID.setPreferredSize(new Dimension(200, 20));
		subpanelPresetID.add(spinnerCreateRoomPresetID, BorderLayout.EAST);

		// ** Save button
		JButton btnPresetSave = new JButton(getUIText("CreateRoom_PresetSave"));
		btnPresetSave.setAlignmentX(0f);
		btnPresetSave.addActionListener(this);
		btnPresetSave.setActionCommand("CreateRoom_PresetSave");
		btnPresetSave.setMnemonic('S');
		btnPresetSave.setMaximumSize(new Dimension(Short.MAX_VALUE, btnPresetSave.getMaximumSize().height));
		containerpanelCreateRoomPreset.add(btnPresetSave);

		// ** Load button
		JButton btnPresetLoad = new JButton(getUIText("CreateRoom_PresetLoad"));
		btnPresetLoad.setAlignmentX(0f);
		btnPresetLoad.addActionListener(this);
		btnPresetLoad.setActionCommand("CreateRoom_PresetLoad");
		btnPresetLoad.setMnemonic('L');
		btnPresetLoad.setMaximumSize(new Dimension(Short.MAX_VALUE, btnPresetLoad.getMaximumSize().height));
		containerpanelCreateRoomPreset.add(btnPresetLoad);

		// ** Preset code panel
		JPanel subpanelPresetCode = new JPanel(new BorderLayout());
		subpanelPresetCode.setAlignmentX(0f);
		containerpanelCreateRoomPreset.add(subpanelPresetCode);

		// *** "Preset code:" Label
		JLabel labelPresetCode = new JLabel(getUIText("CreateRoom_PresetCode"));
		subpanelPresetCode.add(labelPresetCode, BorderLayout.WEST);

		// *** Preset code textfield
		txtfldCreateRoomPresetCode = new JTextField();
		txtfldCreateRoomPresetCode.setComponentPopupMenu(new TextComponentPopupMenu(txtfldCreateRoomPresetCode));
		subpanelPresetCode.add(txtfldCreateRoomPresetCode, BorderLayout.CENTER);

		// *** Preset code export
		JButton btnPresetCodeExport = new JButton(getUIText("CreateRoom_PresetCodeExport"));
		btnPresetCodeExport.setAlignmentX(0f);
		btnPresetCodeExport.addActionListener(this);
		btnPresetCodeExport.setActionCommand("CreateRoom_PresetCodeExport");
		btnPresetCodeExport.setMnemonic('E');
		btnPresetCodeExport.setMaximumSize(new Dimension(Short.MAX_VALUE, btnPresetCodeExport.getMaximumSize().height));
		containerpanelCreateRoomPreset.add(btnPresetCodeExport);

		// *** Preset code import
		JButton btnPresetCodeImport = new JButton(getUIText("CreateRoom_PresetCodeImport"));
		btnPresetCodeImport.setAlignmentX(0f);
		btnPresetCodeImport.addActionListener(this);
		btnPresetCodeImport.setActionCommand("CreateRoom_PresetCodeImport");
		btnPresetCodeImport.setMnemonic('I');
		btnPresetCodeImport.setMaximumSize(new Dimension(Short.MAX_VALUE, btnPresetCodeImport.getMaximumSize().height));
		containerpanelCreateRoomPreset.add(btnPresetCodeImport);

		// buttons

		// **  buttonPanel type
		JPanel subpanelButtons = new JPanel();
		subpanelButtons.setLayout(new BoxLayout(subpanelButtons, BoxLayout.X_AXIS));
		//containerpanelCreateRoom.add(subpanelButtons);
		mainpanelCreateRoom.add(subpanelButtons, BorderLayout.SOUTH);

		// *** OK button
		btnCreateRoomOK = new JButton(getUIText("CreateRoom_OK"));
		btnCreateRoomOK.addActionListener(this);
		btnCreateRoomOK.setActionCommand("CreateRoom_OK");
		btnCreateRoomOK.setMnemonic('O');
		btnCreateRoomOK.setMaximumSize(new Dimension(Short.MAX_VALUE, btnCreateRoomOK.getMaximumSize().height));
		subpanelButtons.add(btnCreateRoomOK);

		// *** Participation in a war button
		btnCreateRoomJoin = new JButton(getUIText("CreateRoom_Join"));
		btnCreateRoomJoin.addActionListener(this);
		btnCreateRoomJoin.setActionCommand("CreateRoom_Join");
		btnCreateRoomJoin.setMnemonic('J');
		btnCreateRoomJoin.setMaximumSize(new Dimension(Short.MAX_VALUE, btnCreateRoomJoin.getMaximumSize().height));
		subpanelButtons.add(btnCreateRoomJoin);

		// *** Participation in a war button
		btnCreateRoomWatch = new JButton(getUIText("CreateRoom_Watch"));
		btnCreateRoomWatch.addActionListener(this);
		btnCreateRoomWatch.setActionCommand("CreateRoom_Watch");
		btnCreateRoomWatch.setMnemonic('W');
		btnCreateRoomWatch.setMaximumSize(new Dimension(Short.MAX_VALUE, btnCreateRoomWatch.getMaximumSize().height));
		subpanelButtons.add(btnCreateRoomWatch);

		// *** Cancel Button
		btnCreateRoomCancel = new JButton(getUIText("CreateRoom_Cancel"));
		btnCreateRoomCancel.addActionListener(this);
		btnCreateRoomCancel.setActionCommand("CreateRoom_Cancel");
		btnCreateRoomCancel.setMnemonic('C');
		btnCreateRoomCancel.setMaximumSize(new Dimension(Short.MAX_VALUE, btnCreateRoomCancel.getMaximumSize().height));
		subpanelButtons.add(btnCreateRoomCancel);
	}

	/**
	 * Create room (1P) screen initialization
	 */
	protected void initCreateRoom1PUI() {
		// Main panel for Create room 1P
		JPanel mainpanelCreateRoom1P = new JPanel();
		mainpanelCreateRoom1P.setLayout(new BoxLayout(mainpanelCreateRoom1P, BoxLayout.Y_AXIS));
		this.getContentPane().add(mainpanelCreateRoom1P, SCREENCARD_NAMES[SCREENCARD_CREATEROOM1P]);

		// * Game mode panel
		JPanel pModeList = new JPanel(new BorderLayout());
		mainpanelCreateRoom1P.add(pModeList);

		labelCreateRoom1PGameMode = new JLabel(getUIText("CreateRoom1P_Mode_Label"));
		pModeList.add(labelCreateRoom1PGameMode, BorderLayout.NORTH);

		// ** Game mode listbox
		listmodelCreateRoom1PModeList = new DefaultListModel();
		loadModeList(listmodelCreateRoom1PModeList, "config/list/netlobby_singlemode.lst");

		listboxCreateRoom1PModeList = new JList(listmodelCreateRoom1PModeList);
		listboxCreateRoom1PModeList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String strMode = (String)listboxCreateRoom1PModeList.getSelectedValue();
				labelCreateRoom1PGameMode.setText(getModeDesc(strMode));
			}
		});
		listboxCreateRoom1PModeList.setSelectedValue(propConfig.getProperty("createroom1p.listboxCreateRoom1PModeList.value", ""), true);
		JScrollPane spCreateRoom1PModeList = new JScrollPane(listboxCreateRoom1PModeList);
		pModeList.add(spCreateRoom1PModeList, BorderLayout.CENTER);

		// * Rule list panel
		JPanel pRuleList = new JPanel(new BorderLayout());
		mainpanelCreateRoom1P.add(pRuleList);

		// ** "Rule:" label
		JLabel lCreateRoom1PRuleList = new JLabel(getUIText("CreateRoom1P_Rule_Label"));
		pRuleList.add(lCreateRoom1PRuleList, BorderLayout.NORTH);

		// ** Rule list listbox
		listmodelCreateRoom1PRuleList = new DefaultListModel();
		listboxCreateRoom1PRuleList = new JList(listmodelCreateRoom1PRuleList);
		JScrollPane spCreateRoom1PRuleList = new JScrollPane(listboxCreateRoom1PRuleList);
		pRuleList.add(spCreateRoom1PRuleList, BorderLayout.CENTER);

		// * Buttons panel
		JPanel subpanelButtons = new JPanel();
		subpanelButtons.setLayout(new BoxLayout(subpanelButtons, BoxLayout.X_AXIS));
		mainpanelCreateRoom1P.add(subpanelButtons);

		// ** OK button
		btnCreateRoom1POK = new JButton(getUIText("CreateRoom1P_OK"));
		btnCreateRoom1POK.addActionListener(this);
		btnCreateRoom1POK.setActionCommand("CreateRoom1P_OK");
		btnCreateRoom1POK.setMnemonic('O');
		btnCreateRoom1POK.setMaximumSize(new Dimension(Short.MAX_VALUE, btnCreateRoom1POK.getMaximumSize().height));
		subpanelButtons.add(btnCreateRoom1POK);

		// ** Cancel button
		btnCreateRoom1PCancel = new JButton(getUIText("CreateRoom1P_Cancel"));
		btnCreateRoom1PCancel.addActionListener(this);
		btnCreateRoom1PCancel.setActionCommand("CreateRoom1P_Cancel");
		btnCreateRoom1PCancel.setMnemonic('C');
		btnCreateRoom1PCancel.setMaximumSize(new Dimension(Short.MAX_VALUE, btnCreateRoom1PCancel.getMaximumSize().height));
		subpanelButtons.add(btnCreateRoom1PCancel);
	}

	/**
	 * MPRanking screen initialization
	 */
	protected void initMPRankingUI() {
		// Main panel for MPRanking
		JPanel mainpanelMPRanking = new JPanel(new BorderLayout());
		this.getContentPane().add(mainpanelMPRanking, SCREENCARD_NAMES[SCREENCARD_MPRANKING]);

		// * Tab
		tabMPRanking = new JTabbedPane();
		mainpanelMPRanking.add(tabMPRanking, BorderLayout.CENTER);

		// ** Leaderboard Table
		strMPRankingTableColumnNames = new String[MPRANKING_COLUMNNAMES.length];
		for(int i = 0; i < strMPRankingTableColumnNames.length; i++) {
			strMPRankingTableColumnNames[i] = getUIText(MPRANKING_COLUMNNAMES[i]);
		}

		tableMPRanking = new JTable[GameEngine.MAX_GAMESTYLE];
		tablemodelMPRanking = new DefaultTableModel[GameEngine.MAX_GAMESTYLE];

		for(int i = 0; i < GameEngine.MAX_GAMESTYLE; i++) {
			tablemodelMPRanking[i] = new DefaultTableModel(strMPRankingTableColumnNames, 0);

			tableMPRanking[i] = new JTable(tablemodelMPRanking[i]);
			tableMPRanking[i].setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tableMPRanking[i].setDefaultEditor(Object.class, null);
			tableMPRanking[i].setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			tableMPRanking[i].getTableHeader().setReorderingAllowed(false);
			tableMPRanking[i].setComponentPopupMenu(new TablePopupMenu(tableMPRanking[i]));

			TableColumnModel tm = tableMPRanking[i].getColumnModel();
			tm.getColumn(0).setPreferredWidth(propConfig.getProperty("tableMPRanking.width.rank", 30));	// Rank
			tm.getColumn(1).setPreferredWidth(propConfig.getProperty("tableMPRanking.width.name", 200));	// Name
			tm.getColumn(2).setPreferredWidth(propConfig.getProperty("tableMPRanking.width.rating", 60));	// Rating
			tm.getColumn(3).setPreferredWidth(propConfig.getProperty("tableMPRanking.width.play", 60));	// Play
			tm.getColumn(4).setPreferredWidth(propConfig.getProperty("tableMPRanking.width.win", 60));	// Win

			JScrollPane spMPRanking = new JScrollPane(tableMPRanking[i]);
			tabMPRanking.addTab(GameEngine.GAMESTYLE_NAMES[i], spMPRanking);

			if(i != GameEngine.GAMESTYLE_TETROMINO) {
				tabMPRanking.setEnabledAt(i, false);	// TODO: Add non-tetromino leaderboard
			}
		}

		// * OK Button
		btnMPRankingOK = new JButton(getUIText("MPRanking_OK"));
		btnMPRankingOK.addActionListener(this);
		btnMPRankingOK.setActionCommand("MPRanking_OK");
		btnMPRankingOK.setMnemonic('O');
		mainpanelMPRanking.add(btnMPRankingOK, BorderLayout.SOUTH);
	}

	/**
	 * Rule change screen initialization
	 */
	protected void initRuleChangeUI() {
		// Main panel for RuleChange
		JPanel mainpanelRuleChange = new JPanel(new BorderLayout());
		this.getContentPane().add(mainpanelRuleChange, SCREENCARD_NAMES[SCREENCARD_RULECHANGE]);

		// * Tab
		tabRuleChange = new JTabbedPane();
		mainpanelRuleChange.add(tabRuleChange, BorderLayout.CENTER);

		// ** Rule Listboxes
		listboxRuleChangeRuleList = new JList[GameEngine.MAX_GAMESTYLE];
		for(int i = 0; i < GameEngine.MAX_GAMESTYLE; i++) {
			listboxRuleChangeRuleList[i] = new JList(extractRuleListFromRuleEntries(i));
			JScrollPane spRuleList = new JScrollPane(listboxRuleChangeRuleList[i]);
			tabRuleChange.addTab(GameEngine.GAMESTYLE_NAMES[i], spRuleList);
		}

		// ** Tuning Tab
		JPanel subpanelTuning = new JPanel();
		subpanelTuning.setLayout(new BoxLayout(subpanelTuning, BoxLayout.Y_AXIS));
		tabRuleChange.addTab(getUIText("RuleChange_Tab_Tuning"), subpanelTuning);

		// *** A button rotate
		JPanel pTuningRotateButtonDefaultRight = new JPanel();
		//pTuningRotateButtonDefaultRight.setLayout(new BoxLayout(pTuningRotateButtonDefaultRight, BoxLayout.Y_AXIS));
		pTuningRotateButtonDefaultRight.setAlignmentX(LEFT_ALIGNMENT);
		subpanelTuning.add(pTuningRotateButtonDefaultRight);

		JLabel lTuningRotateButtonDefaultRight = new JLabel(getUIText("GameTuning_RotateButtonDefaultRight_Label"));
		pTuningRotateButtonDefaultRight.add(lTuningRotateButtonDefaultRight);

		String[] strArrayTuningRotateButtonDefaultRight = new String[TUNING_ABUTTON_ROTATE.length];
		for(int i = 0; i < TUNING_ABUTTON_ROTATE.length; i++) {
			strArrayTuningRotateButtonDefaultRight[i] = getUIText(TUNING_ABUTTON_ROTATE[i]);
		}
		comboboxTuningRotateButtonDefaultRight = new JComboBox(strArrayTuningRotateButtonDefaultRight);
		pTuningRotateButtonDefaultRight.add(comboboxTuningRotateButtonDefaultRight);

		// *** Diagonal move
		JPanel pTuningMoveDiagonal = new JPanel();
		pTuningMoveDiagonal.setAlignmentX(LEFT_ALIGNMENT);
		subpanelTuning.add(pTuningMoveDiagonal);

		JLabel lTuningMoveDiagonal = new JLabel(getUIText("GameTuning_MoveDiagonal_Label"));
		pTuningMoveDiagonal.add(lTuningMoveDiagonal);

		String[] strArrayTuningMoveDiagonal = new String[TUNING_COMBOBOX_GENERIC.length];
		for(int i = 0; i < TUNING_COMBOBOX_GENERIC.length; i++) {
			strArrayTuningMoveDiagonal[i] = getUIText(TUNING_COMBOBOX_GENERIC[i]);
		}
		comboboxTuningMoveDiagonal = new JComboBox(strArrayTuningMoveDiagonal);
		pTuningMoveDiagonal.add(comboboxTuningMoveDiagonal);

		// *** Show Outline Only
		JPanel pTuningBlockShowOutlineOnly = new JPanel();
		pTuningBlockShowOutlineOnly.setAlignmentX(LEFT_ALIGNMENT);
		subpanelTuning.add(pTuningBlockShowOutlineOnly);

		JLabel lTuningBlockShowOutlineOnly = new JLabel(getUIText("GameTuning_BlockShowOutlineOnly_Label"));
		pTuningBlockShowOutlineOnly.add(lTuningBlockShowOutlineOnly);

		String[] strArrayTuningBlockShowOutlineOnly = new String[TUNING_COMBOBOX_GENERIC.length];
		for(int i = 0; i < TUNING_COMBOBOX_GENERIC.length; i++) {
			strArrayTuningBlockShowOutlineOnly[i] = getUIText(TUNING_COMBOBOX_GENERIC[i]);
		}
		comboboxTuningBlockShowOutlineOnly = new JComboBox(strArrayTuningBlockShowOutlineOnly);
		pTuningBlockShowOutlineOnly.add(comboboxTuningBlockShowOutlineOnly);

		// *** Outline Type
		JPanel pTuningOutlineType = new JPanel();
		pTuningOutlineType.setAlignmentX(LEFT_ALIGNMENT);
		subpanelTuning.add(pTuningOutlineType);

		JLabel lTuningOutlineType = new JLabel(getUIText("GameTuning_OutlineType_Label"));
		pTuningOutlineType.add(lTuningOutlineType);

		String[] strArrayTuningOutlineType = new String[TUNING_OUTLINE_TYPE_NAMES.length];
		for(int i = 0; i < TUNING_OUTLINE_TYPE_NAMES.length; i++) {
			strArrayTuningOutlineType[i] = getUIText(TUNING_OUTLINE_TYPE_NAMES[i]);
		}
		DefaultComboBoxModel modelTuningOutlineType = new DefaultComboBoxModel(strArrayTuningOutlineType);
		comboboxTuningBlockOutlineType = new JComboBox(modelTuningOutlineType);
		pTuningOutlineType.add(comboboxTuningBlockOutlineType);

		// *** Skin
		JPanel pTuningSkin = new JPanel();
		pTuningSkin.setAlignmentX(LEFT_ALIGNMENT);
		subpanelTuning.add(pTuningSkin);

		JLabel lTuningSkin = new JLabel(getUIText("GameTuning_Skin_Label"));
		pTuningSkin.add(lTuningSkin);

		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement(new ComboLabel(getUIText("GameTuning_Skin_Auto")));
		for(int i = 0; i < imgTuningBlockSkins.length; i++) {
			model.addElement(new ComboLabel("" + i, new ImageIcon(imgTuningBlockSkins[i])));
		}

		comboboxTuningSkin = new JComboBox(model);
		comboboxTuningSkin.setRenderer(new ComboLabelCellRenderer());
		comboboxTuningSkin.setPreferredSize(new Dimension(190, 30));
		pTuningSkin.add(comboboxTuningSkin);

		// *** Minimum DAS
		JPanel pTuningMinDAS = new JPanel();
		pTuningMinDAS.setAlignmentX(LEFT_ALIGNMENT);
		subpanelTuning.add(pTuningMinDAS);

		JLabel lTuningMinDAS = new JLabel(getUIText("GameTuning_MinDAS_Label"));
		pTuningMinDAS.add(lTuningMinDAS);

		txtfldTuningMinDAS = new JTextField(5);
		pTuningMinDAS.add(txtfldTuningMinDAS);

		// *** Maximum DAS
		JPanel pTuningMaxDAS = new JPanel();
		pTuningMaxDAS.setAlignmentX(LEFT_ALIGNMENT);
		subpanelTuning.add(pTuningMaxDAS);

		JLabel lTuningMaxDAS = new JLabel(getUIText("GameTuning_MaxDAS_Label"));
		pTuningMaxDAS.add(lTuningMaxDAS);

		txtfldTuningMaxDAS = new JTextField(5);
		pTuningMaxDAS.add(txtfldTuningMaxDAS);

		// *** DAS delay
		JPanel pTuningDasDelay = new JPanel();
		pTuningDasDelay.setAlignmentX(LEFT_ALIGNMENT);
		subpanelTuning.add(pTuningDasDelay);

		JLabel lTuningDasDelay = new JLabel(getUIText("GameTuning_DasDelay_Label"));
		pTuningDasDelay.add(lTuningDasDelay);

		txtfldTuningDasDelay = new JTextField(5);
		pTuningDasDelay.add(txtfldTuningDasDelay);

		// *** Reverse Up/Down
		JPanel pTuningReverseUpDown = new JPanel();
		pTuningReverseUpDown.setAlignmentX(LEFT_ALIGNMENT);
		subpanelTuning.add(pTuningReverseUpDown);

		JLabel lTuningReverseUpDown = new JLabel(getUIText("GameTuning_ReverseUpDown_Label"));
		pTuningReverseUpDown.add(lTuningReverseUpDown);

		chkboxTuningReverseUpDown = new JCheckBox();
		pTuningReverseUpDown.add(chkboxTuningReverseUpDown);

		// * Buttons panel
		JPanel subpanelButtons = new JPanel();
		subpanelButtons.setLayout(new BoxLayout(subpanelButtons, BoxLayout.X_AXIS));
		mainpanelRuleChange.add(subpanelButtons, BorderLayout.SOUTH);

		// ** OK button
		btnRuleChangeOK = new JButton(getUIText("RuleChange_OK"));
		btnRuleChangeOK.addActionListener(this);
		btnRuleChangeOK.setActionCommand("RuleChange_OK");
		btnRuleChangeOK.setMnemonic('O');
		btnRuleChangeOK.setMaximumSize(new Dimension(Short.MAX_VALUE, btnRuleChangeOK.getMaximumSize().height));
		subpanelButtons.add(btnRuleChangeOK);

		// ** Cancel button
		btnRuleChangeCancel = new JButton(getUIText("RuleChange_Cancel"));
		btnRuleChangeCancel.addActionListener(this);
		btnRuleChangeCancel.setActionCommand("RuleChange_Cancel");
		btnRuleChangeCancel.setMnemonic('C');
		btnRuleChangeCancel.setMaximumSize(new Dimension(Short.MAX_VALUE, btnRuleChangeCancel.getMaximumSize().height));
		subpanelButtons.add(btnRuleChangeCancel);
	}

	/**
	 * Load block skins
	 */
	protected void loadBlockSkins() {
		String skindir = propGlobal.getProperty("custom.skin.directory", "res");

		int numSkins = 0;
		File file = null;
		while(true) {
			file = new File(skindir + "/graphics/blockskin/normal/n" + numSkins + ".png");
			if(file.canRead()) {
				numSkins++;
			} else {
				break;
			}
		}
		log.debug(numSkins + " block skins found");

		imgTuningBlockSkins = new BufferedImage[numSkins];

		for(int i = 0; i < numSkins; i++) {
			BufferedImage imgBlock = (BufferedImage) loadImage(getURL(skindir + "/graphics/blockskin/normal/n" + i + ".png"));
			boolean isSticky = ((imgBlock != null) && (imgBlock.getWidth() >= 400) && (imgBlock.getHeight() >= 304));

			imgTuningBlockSkins[i] = new BufferedImage(144, 16, BufferedImage.TYPE_INT_RGB);

			if(isSticky) {
				for(int j = 0; j < 9; j++) {
					imgTuningBlockSkins[i].getGraphics().drawImage(imgBlock, j * 16, 0, (j * 16) + 16, 16, 0, j * 16, 16, (j * 16) + 16, null);
				}
			} else {
				imgTuningBlockSkins[i].getGraphics().drawImage(imgBlock, 0, 0, 144, 16, 0, 0, 144, 16, null);
			}
		}
	}

	/**
	 * Load image file
	 * @param url Image file URL
	 * @return Image data (or null when load fails)
	 */
	protected BufferedImage loadImage(URL url) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(url);
			log.debug("Loaded image from " + url);
		} catch (IOException e) {
			log.error("Failed to load image from " + url, e);
		}
		return img;
	}

	/**
	 * Get the URL of filename
	 * @param str Filename
	 * @return URL of the filename
	 */
	protected URL getURL(String str) {
		URL url = null;

		try {
			char sep = File.separator.charAt(0);
			String file = str.replace(sep, '/');

			if(file.charAt(0) != '/') {
				String dir = System.getProperty("user.dir");
				dir = dir.replace(sep, '/') + '/';
				if(dir.charAt(0) != '/') {
					dir = "/" + dir;
				}
				file = dir + file;
			}
			url = new URL("file", "", file);
		} catch(MalformedURLException e) {
			log.warn("Invalid URL:" + str, e);
			return null;
		}

		return url;
	}

	/**
	 * PosttranslationalUIGets a string of
	 * @param str String
	 * @return PosttranslationalUIString (If you do not acceptstrReturns)
	 */
	public String getUIText(String str) {
		String result = propLang.getProperty(str);
		if(result == null) {
			result = propLangDefault.getProperty(str, str);
		}
		return result;
	}

	/**
	 * Get game mode description
	 * @param str Mode name
	 * @return Description
	 */
	protected String getModeDesc(final String str) {
		String str2 = str.replace(' ', '_');
		str2 = str2.replace('(', 'l');
		str2 = str2.replace(')', 'r');
		String result = propModeDesc.getProperty(str2);
		if(result == null) {
			result = propDefaultModeDesc.getProperty(str2, str2);
		}
		return result;
	}

	/**
	 * Get int value from JTextField
	 * @param value Default Value (used if convertion fails)
	 * @param txtfld JTextField
	 * @return int value (or default value if fails)
	 */
	public static int getIntTextField(int value, JTextField txtfld) {
		int v = value;

		try {
			v = Integer.parseInt(txtfld.getText());
		} catch(NumberFormatException e) {}

		return v;
	}

	/**
	 * Screen switching
	 * @param cardNumber Card switching destination screen number
	 */
	public void changeCurrentScreenCard(int cardNumber) {
		try {
			contentPaneCardLayout.show(this.getContentPane(), SCREENCARD_NAMES[cardNumber]);
			currentScreenCardNumber = cardNumber;

			// Set menu bar
			setJMenuBar(menuBar[cardNumber]);

			// Set default button
			JButton defaultButton = null;
			switch(currentScreenCardNumber) {
			case SCREENCARD_SERVERSELECT:
				defaultButton = btnServerConnect;
				break;
			case SCREENCARD_LOBBY:
				if(tabLobbyAndRoom.getSelectedIndex() == 0)
					defaultButton = btnLobbyChatSend;
				else
					defaultButton = btnRoomChatSend;
				break;
			case SCREENCARD_SERVERADD:
				defaultButton = btnServerAddOK;
				break;
			case SCREENCARD_CREATERATED_WAITING:
				defaultButton = btnCreateRatedWaitingCancel;
				break;
			case SCREENCARD_CREATERATED:
				defaultButton = btnCreateRatedOK;
				break;
			case SCREENCARD_CREATEROOM:
				if (btnCreateRoomOK.isVisible())
					defaultButton = btnCreateRoomOK;
				else
					defaultButton = btnCreateRoomCancel;
				break;
			case SCREENCARD_CREATEROOM1P:
				if(btnCreateRoom1POK.isVisible())
					defaultButton = btnCreateRoom1POK;
				else
					defaultButton = btnCreateRoom1PCancel;
				break;
			case SCREENCARD_MPRANKING:
				defaultButton = btnMPRankingOK;
				break;
			case SCREENCARD_RULECHANGE:
				defaultButton = btnRuleChangeOK;
				break;
			}

			if(defaultButton != null) {
				this.getRootPane().setDefaultButton(defaultButton);
			}
		} catch (Exception e) {
			// TODO: There are several threading issue here
			log.debug("changeCurrentScreenCard failed; Possible threading issue", e);
		}
	}

	/**
	 * @return Current ScreenChat log
	 */
	public JTextPane getCurrentChatLogTextPane() {
		if(tabLobbyAndRoom.getSelectedIndex() != 0) {
			return txtpaneRoomChatLog;
		}
		return txtpaneLobbyChatLog;
	}

	/**
	 * Get current time as String (for chat log)
	 * @return Current time as String
	 */
	public String getCurrentTimeAsString() {
		GregorianCalendar currentTime = new GregorianCalendar();
		DateFormat dfm = new SimpleDateFormat("HH:mm:ss");
		return dfm.format(currentTime.getTime());
	}

	/**
	 * Create String from a Calendar (for chat log)
	 * @param cal Calendar
	 * @return String created from Calendar
	 */
	public String getTimeAsString(Calendar cal) {
		return getTimeAsString(cal, false);
	}

	/**
	 * Create String from a Calendar (for chat log)
	 * @param cal Calendar
	 * @param showDate true to show date
	 * @return String created from Calendar
	 */
	public String getTimeAsString(Calendar cal, boolean showDate) {
		if(cal == null) return showDate ? "????-??-?? ??:??:??" : "??:??:??";
		String strFormat = showDate ? "yyyy-MM-dd HH:mm:ss" : "HH:mm:ss";
		DateFormat dfm = new SimpleDateFormat(strFormat);
		return dfm.format(cal.getTime());
	}

	/**
	 * PlayerOfNameObtained by converting symbol trip
	 * @param pInfo PlayerInformation
	 * @return PlayerOfName(Translated symbol trip)
	 */
	public String getPlayerNameWithTripCode(NetPlayerInfo pInfo) {
		return convTripCode(pInfo.strName);
	}

	/**
	 * Convert the symbol trip
	 * @param s String to be converted(MostName)
	 * @return The converted string
	 */
	public String convTripCode(String s) {
		if(propLang.getProperty("TripSeparator_EnableConvert", false) == false) return s;
		String strName = s;
		strName = strName.replace(getUIText("TripSeparator_True"), getUIText("TripSeparator_False"));
		strName = strName.replace("!", getUIText("TripSeparator_True"));
		strName = strName.replace("?", getUIText("TripSeparator_False"));
		return strName;
	}

	/**
	 * Chat logAdd a new line to the(System Messages)
	 * @param txtpane Chat log
	 * @param str The string to add
	 */
	public void addSystemChatLog(JTextPane txtpane, String str) {
		addSystemChatLog(txtpane, str, null);
	}

	/**
	 * Chat logAdd a new line to the(System Messages)
	 * @param txtpane Chat log
	 * @param str The string to add
	 * @param fgcolor Letter color(nullYes)
	 */
	public void addSystemChatLog(JTextPane txtpane, String str, Color fgcolor) {
		String strTime = getCurrentTimeAsString();

		SimpleAttributeSet sas = null;
		if(fgcolor != null) {
			sas = new SimpleAttributeSet();
			StyleConstants.setForeground(sas, fgcolor);
		}
		try {
			Document doc = txtpane.getDocument();
			doc.insertString(doc.getLength(), str + "\n", sas);
			txtpane.setCaretPosition(doc.getLength());

			if(txtpane == txtpaneRoomChatLog) {
				if(writerRoomLog != null) {
					writerRoomLog.println("[" + strTime + "] " + str);
					writerRoomLog.flush();
				}
			} else if(writerLobbyLog != null) {
				writerLobbyLog.println("[" + strTime + "] " + str);
				writerLobbyLog.flush();
			}
		} catch (Exception e) {}
	}

	/**
	 * Chat logAdd a new line to the(System calls from a different thread for message)
	 * @param txtpane Chat log
	 * @param str The string to add
	 */
	public void addSystemChatLogLater(final JTextPane txtpane, final String str) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				addSystemChatLog(txtpane, str);
			}
		});
	}

	/**
	 * Chat logAdd a new line to the(System calls from a different thread for message)
	 * @param txtpane Chat log
	 * @param str The string to add
	 * @param fgcolor Letter color(nullYes)
	 */
	public void addSystemChatLogLater(final JTextPane txtpane, final String str, final Color fgcolor) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				addSystemChatLog(txtpane, str, fgcolor);
			}
		});
	}

	/**
	 * Add a user chat to log pane
	 * @param txtpane JTextPane to add this chat log
	 * @param username User name
	 * @param calendar Time
	 * @param str Message
	 */
	public void addUserChatLog(JTextPane txtpane, String username, Calendar calendar, String str) {
		SimpleAttributeSet sasTime = new SimpleAttributeSet();
		StyleConstants.setForeground(sasTime, Color.gray);
		String strTime = getTimeAsString(calendar);

		SimpleAttributeSet sas = new SimpleAttributeSet();
		StyleConstants.setBold(sas, true);
		StyleConstants.setUnderline(sas, true);

		try {
			Document doc = txtpane.getDocument();
			doc.insertString(doc.getLength(), "[" + strTime + "]", sasTime);
			doc.insertString(doc.getLength(), "<" + username + ">", sas);
			doc.insertString(doc.getLength(), " " + str + "\n", null);
			txtpane.setCaretPosition(doc.getLength());

			if(txtpane == txtpaneRoomChatLog) {
				if(writerRoomLog != null) {
					writerRoomLog.println("[" + strTime + "]" + "<" + username + "> " + str);
					writerRoomLog.flush();
				}
			} else if(writerLobbyLog != null) {
				writerLobbyLog.println("[" + strTime + "]" + "<" + username + "> " + str);
				writerLobbyLog.flush();
			}
		} catch (Exception e) {}
	}

	/**
	 * Add a user chat to log pane (for multi threading)
	 * @param txtpane JTextPane to add this chat log
	 * @param username User name
	 * @param calendar Time
	 * @param str Message
	 */
	public void addUserChatLogLater(final JTextPane txtpane, final String username, final Calendar calendar, final String str) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				addUserChatLog(txtpane, username, calendar, str);
			}
		});
	}

	/**
	 * Add a recorded user chat to log pane
	 * @param txtpane JTextPane to add this chat log
	 * @param username User name
	 * @param calendar Time
	 * @param str Message
	 */
	public void addRecordedUserChatLog(JTextPane txtpane, String username, Calendar calendar, String str) {
		SimpleAttributeSet sasTime = new SimpleAttributeSet();
		StyleConstants.setForeground(sasTime, Color.gray);
		String strTime = getTimeAsString(calendar, true);

		SimpleAttributeSet sasUserName = new SimpleAttributeSet();
		StyleConstants.setBold(sasUserName, true);
		StyleConstants.setUnderline(sasUserName, true);
		StyleConstants.setForeground(sasUserName, Color.gray);

		SimpleAttributeSet sasMessage = new SimpleAttributeSet();
		StyleConstants.setForeground(sasMessage, Color.darkGray);

		try {
			Document doc = txtpane.getDocument();
			doc.insertString(doc.getLength(), "[" + strTime + "]", sasTime);
			doc.insertString(doc.getLength(), "<" + username + ">", sasUserName);
			doc.insertString(doc.getLength(), " " + str + "\n", sasMessage);
			txtpane.setCaretPosition(doc.getLength());

			if(txtpane == txtpaneRoomChatLog) {
				if(writerRoomLog != null) {
					writerRoomLog.println("[" + strTime + "]" + "<" + username + "> " + str);
					writerRoomLog.flush();
				}
			} else if(writerLobbyLog != null) {
				writerLobbyLog.println("[" + strTime + "]" + "<" + username + "> " + str);
				writerLobbyLog.flush();
			}
		} catch (Exception e) {}
	}

	/**
	 * Add a recorded user chat to log pane (for multi threading)
	 * @param txtpane JTextPane to add this chat log
	 * @param username User name
	 * @param calendar Time
	 * @param str Message
	 */
	public void addRecordedUserChatLogLater(final JTextPane txtpane, final String username, final Calendar calendar, final String str) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				addRecordedUserChatLog(txtpane, username, calendar, str);
			}
		});
	}

	/**
	 * FileDefaultListModelRead on
	 * @param listModel Which to readDefaultListModel
	 * @param filename Filename
	 * @return The successtrue
	 */
	public boolean loadListToDefaultListModel(DefaultListModel listModel, String filename) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			listModel.clear();

			String str = null;
			while((str = in.readLine()) != null) {
				if(str.length() > 0)
					listModel.addElement(str);
			}
		} catch (IOException e) {
			log.debug("Failed to load list from " + filename, e);
			return false;
		}
		return true;
	}

	/**
	 * Load game mode list to a DefaultListModel
	 * @param listModel DefaultListModel
	 * @param filename Filename of mode list
	 * @return <code>true</code> if success
	 */
	public boolean loadModeList(DefaultListModel listModel, String filename) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			listModel.clear();

			String str = null;
			while((str = in.readLine()) != null) {
				if((str.length() <= 0) || str.startsWith("#")) {
					// Empty line or comment line. Ignore it.
				} else if(str.startsWith(":")) {
					// Game style tag. Currently unused.
				} else {
					// Game mode name
					int commaIndex = str.indexOf(',');
					if(commaIndex != -1) {
						listModel.addElement(str.substring(0, commaIndex));
					}
				}
			}
		} catch (IOException e) {
			log.debug("Failed to load list from " + filename, e);
			return false;
		}
		return true;
	}

	/**
	 * Load game mode list to a DefaultComboBoxModel
	 * @param listModel DefaultComboBoxModel
	 * @param filename Filename of mode list
	 * @return <code>true</code> if success
	 */
	public boolean loadModeList(DefaultComboBoxModel listModel, String filename) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));

			listModel.removeAllElements();

			String str = null;
			while((str = in.readLine()) != null) {
				if((str.length() <= 0) || str.startsWith("#")) {
					// Empty line or comment line. Ignore it.
				} else if(str.startsWith(":")) {
					// Game style tag. Currently unused.
				} else {
					// Game mode name
					int commaIndex = str.indexOf(',');
					if(commaIndex != -1) {
						listModel.addElement(str.substring(0, commaIndex));
					}
				}
			}
		} catch (IOException e) {
			log.debug("Failed to load list from " + filename, e);
			return false;
		}
		return true;
	}

	/**
	 * DefaultListModelSave it to a file from
	 * @param listModel Preservation of the originalDefaultListModel
	 * @param filename Filename
	 * @return The successtrue
	 */
	public boolean saveListFromDefaultListModel(DefaultListModel listModel, String filename) {
		try {
			PrintWriter out = new PrintWriter(filename);
			for(int i = 0; i < listModel.size(); i++) {
				out.println(listModel.get(i));
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			log.debug("Failed to save server list", e);
			return false;
		}
		return true;
	}

	/**
	 * Set enabled state of lobby buttons
	 * @param mode 0=Disable all 1=Full Lobby 2=Inside Room
	 */
	public void setLobbyButtonsEnabled(int mode) {
		btnRoomListQuickStart.setEnabled(mode == 1);
		btnRoomListRoomCreate.setEnabled(mode == 1);
		btnRoomListRoomCreate1P.setEnabled(mode == 1);

		itemLobbyMenuRuleChange.setEnabled(mode == 1);
		itemLobbyMenuTeamChange.setEnabled(mode >= 1);
		itemLobbyMenuRanking.setEnabled(mode >= 1);

		btnLobbyChatSend.setEnabled(mode >= 1);
		txtfldLobbyChatInput.setEnabled(mode >= 1);
	}

	/**
	 * Screen Room buttonOf is enabledChange the state
	 * @param b When true, is enabled, falseInvalid if
	 */
	public void setRoomButtonsEnabled(boolean b) {
		btnRoomButtonsTeamChange.setEnabled(b);
		btnRoomButtonsJoin.setEnabled(b);
		btnRoomButtonsSitOut.setEnabled(b);
		btnRoomButtonsViewSetting.setEnabled(b);
		btnRoomButtonsRanking.setEnabled(b);
		btnRoomChatSend.setEnabled(b);
		txtfldRoomChatInput.setEnabled(b);
	}

	/**
	 * Participation in the screen room buttonAnd withdrawal buttonSwitching
	 * @param b trueWhen the war button, falseWhen the withdrawal buttonShow
	 */
	public void setRoomJoinButtonVisible(boolean b) {
		btnRoomButtonsJoin.setVisible(b);
		btnRoomButtonsJoin.setEnabled(true);
		btnRoomButtonsSitOut.setVisible(!b);
		btnRoomButtonsSitOut.setEnabled(true);
	}

	/**
	 * Room list tableRow dataCreate
	 * @param r Room Information
	 * @return Line data
	 */
	public String[] createRoomListRowData(NetRoomInfo r) {
		String[] rowData = new String[8];
		rowData[0] = Integer.toString(r.roomID);
		rowData[1] = r.strName;
		rowData[2] = r.rated ? getUIText("RoomTable_Rated_True") : getUIText("RoomTable_Rated_False");
		rowData[3] = r.ruleLock ? r.ruleName.toUpperCase() : getUIText("RoomTable_RuleName_Any");
		rowData[4] = r.strMode;
		rowData[5] = r.playing ? getUIText("RoomTable_Status_Playing") : getUIText("RoomTable_Status_Waiting");
		rowData[6] = r.playerSeatedCount + "/" + r.maxPlayers;
		rowData[7] = Integer.toString(r.spectatorCount);
		return rowData;
	}

	/**
	 * Entered the room that you specify
	 * @param roomID RoomID
	 * @param watch When true,Watching only
	 */
	public void joinRoom(int roomID, boolean watch) {
		tabLobbyAndRoom.setEnabledAt(1, true);
		tabLobbyAndRoom.setSelectedIndex(1);

		if(netPlayerClient.getYourPlayerInfo().roomID != roomID) {
			txtpaneRoomChatLog.setText("");
			setRoomButtonsEnabled(false);
			netPlayerClient.send("roomjoin\t" + roomID + "\t" + watch + "\n");
		}

		changeCurrentScreenCard(SCREENCARD_LOBBY);
	}

	/**
	 * Change UI type of multiplayer room create screen
	 * @param isDetailMode true=View Detail, false=Create Room
	 * @param roomInfo Room Information (only used when isDetailMode is true)
	 */
	public void setCreateRoomUIType(boolean isDetailMode, NetRoomInfo roomInfo) {
		NetRoomInfo r = null;

		if(isDetailMode) {
			btnCreateRoomOK.setVisible(false);
			btnCreateRoomJoin.setVisible(true);
			btnCreateRoomWatch.setVisible(true);
			r = roomInfo;

			if(netPlayerClient.getYourPlayerInfo().roomID == r.roomID) {
				btnCreateRoomJoin.setVisible(false);
				btnCreateRoomWatch.setVisible(false);
			}
		} else {
			btnCreateRoomOK.setVisible(true);
			btnCreateRoomJoin.setVisible(false);
			btnCreateRoomWatch.setVisible(false);

			if(backupRoomInfo != null) {
				r = backupRoomInfo;
			} else {
				r = new NetRoomInfo();
				r.maxPlayers = propConfig.getProperty("createroom.defaultMaxPlayers", 6);
				r.autoStartSeconds = propConfig.getProperty("createroom.defaultAutoStartSeconds", 15);
				r.gravity = propConfig.getProperty("createroom.defaultGravity", 1);
				r.denominator = propConfig.getProperty("createroom.defaultDenominator", 60);
				r.are = propConfig.getProperty("createroom.defaultARE", 0);
				r.areLine = propConfig.getProperty("createroom.defaultARELine", 0);
				r.lineDelay = propConfig.getProperty("createroom.defaultLineDelay", 0);
				r.lockDelay = propConfig.getProperty("createroom.defaultLockDelay", 30);
				r.das = propConfig.getProperty("createroom.defaultDAS", 11);
				r.hurryupSeconds = propConfig.getProperty("createroom.defaultHurryupSeconds", 180);
				r.hurryupInterval = propConfig.getProperty("createroom.defaultHurryupInterval", 5);
				r.garbagePercent = propConfig.getProperty("createroom.defaultGarbagePercent", 90);
				r.ruleLock = propConfig.getProperty("createroom.defaultRuleLock", false);
				r.tspinEnableType = propConfig.getProperty("createroom.defaultTSpinEnableType", 1);
				r.spinCheckType = propConfig.getProperty("createroom.defaultSpinCheckType", 0);
				r.tspinEnableEZ = propConfig.getProperty("createroom.defaultTSpinEnableEZ", false);
				r.b2b = propConfig.getProperty("createroom.defaultB2B", true);
				r.combo = propConfig.getProperty("createroom.defaultCombo", true);
				r.rensaBlock = propConfig.getProperty("createroom.defaultRensaBlock", true);
				r.counter = propConfig.getProperty("createroom.defaultCounter", true);
				r.bravo = propConfig.getProperty("createroom.defaultBravo", true);
				r.reduceLineSend = propConfig.getProperty("createroom.defaultReduceLineSend", true);
				r.garbageChangePerAttack = propConfig.getProperty("createroom.defaultGarbageChangePerAttack", true);
				r.b2bChunk = propConfig.getProperty("createroom.defaultB2BChunk", false);
				r.useFractionalGarbage = propConfig.getProperty("createroom.defaultUseFractionalGarbage", false);
				r.autoStartTNET2 = propConfig.getProperty("createroom.defaultAutoStartTNET2", false);
				r.disableTimerAfterSomeoneCancelled = propConfig.getProperty("createroom.defaultDisableTimerAfterSomeoneCancelled", false);
				r.useMap = propConfig.getProperty("createroom.defaultUseMap", false);
				//propConfig.getProperty("createroom.defaultMapSetID", 0);
				r.ruleName = propConfig.getProperty("createroom.ruleName", "");
			}
		}

		importRoomInfoToCreateRoomScreen(r);
	}

	/**
	 * Change UI type of single player room create screen
	 * @param isDetailMode true=View Detail, false=Create Room
	 * @param roomInfo Room Information (only used when isDetailMode is true)
	 */
	public void setCreateRoom1PUIType(boolean isDetailMode, NetRoomInfo roomInfo) {
		NetRoomInfo r = null;

		if(isDetailMode) {
			r = roomInfo;

			if(netPlayerClient.getYourPlayerInfo().roomID == r.roomID) {
				btnCreateRoom1POK.setVisible(false);
			} else {
				btnCreateRoom1POK.setVisible(true);
				btnCreateRoom1POK.setText(getUIText("CreateRoom1P_OK_Watch"));
				btnCreateRoom1POK.setMnemonic('W');
			}
		} else {
			btnCreateRoom1POK.setVisible(true);
			btnCreateRoom1POK.setText(getUIText("CreateRoom1P_OK"));
			btnCreateRoom1POK.setMnemonic('O');

			if(backupRoomInfo1P != null) {
				r = backupRoomInfo1P;
			} else {
				r = new NetRoomInfo();
				r.maxPlayers = 1;
				r.singleplayer = true;
				r.strMode = propConfig.getProperty("createroom1p.strMode", "");
				r.ruleName = propConfig.getProperty("createroom1p.ruleName", "");
			}
		}

		if(r != null) {
			//listboxCreateRoom1PModeList.setSelectedIndex(0);
			//listboxCreateRoom1PRuleList.setSelectedIndex(0);
			listboxCreateRoom1PModeList.setSelectedValue(r.strMode, true);
			listboxCreateRoom1PRuleList.setSelectedValue(r.ruleName, true);
		}
	}

	/**
	 * Switch to room detail screen
	 * @param roomID Room ID
	 */
	public void viewRoomDetail(int roomID) {
		NetRoomInfo roomInfo = netPlayerClient.getRoomInfo(roomID);

		if(roomInfo != null) {
			currentViewDetailRoomID = roomID;

			if(roomInfo.singleplayer) {
				setCreateRoom1PUIType(true, roomInfo);
				changeCurrentScreenCard(SCREENCARD_CREATEROOM1P);
			} else {
				setCreateRoomUIType(true, roomInfo);
				changeCurrentScreenCard(SCREENCARD_CREATEROOM);
			}
		}
	}

	/**
	 * Lobby screenPlayerList update
	 */
	public void updateLobbyUserList() {
		LinkedList<NetPlayerInfo> pList = new LinkedList<NetPlayerInfo>(netPlayerClient.getPlayerInfoList());

		if(!pList.isEmpty()) {
			listmodelLobbyChatPlayerList.clear();

			for(int i = 0; i < pList.size(); i++) {
				NetPlayerInfo pInfo = pList.get(i);

				// Name
				String name = getPlayerNameWithTripCode(pInfo);
				if(pInfo.uid == netPlayerClient.getPlayerUID()) name = "*" + getPlayerNameWithTripCode(pInfo);

				// Team
				if(pInfo.strTeam.length() > 0) {
					name = getPlayerNameWithTripCode(pInfo) + " - " + pInfo.strTeam;
					if(pInfo.uid == netPlayerClient.getPlayerUID()) name = "*" + getPlayerNameWithTripCode(pInfo) + " - " + pInfo.strTeam;
				}

				// Rating
				name += " |" + pInfo.rating[0] + "|";
				/*
				name += " |T:" + pInfo.rating[0] + "|";
				name += "A:" + pInfo.rating[1] + "|";
				name += "P:" + pInfo.rating[2] + "|";
				name += "S:" + pInfo.rating[3] + "|";
				*/

				// Country code
				if(pInfo.strCountry.length() > 0) {
					name += " (" + pInfo.strCountry + ")";
				}

				/* XXX Hostname
				if(pInfo.strHost.length() > 0) {
					name += " {" + pInfo.strHost + "}";
				}
				*/

				if(pInfo.roomID == -1) {
					listmodelLobbyChatPlayerList.addElement(name);
				} else {
					listmodelLobbyChatPlayerList.addElement("{" + pInfo.roomID + "} " + name);
				}
			}
		}
	}

	/**
	 * Screen RoomPlayerList update
	 */
	public void updateRoomUserList() {
		NetRoomInfo roomInfo = netPlayerClient.getRoomInfo(netPlayerClient.getYourPlayerInfo().roomID);
		if(roomInfo == null) return;

		LinkedList<NetPlayerInfo> pList = new LinkedList<NetPlayerInfo>(netPlayerClient.getPlayerInfoList());

		if(!pList.isEmpty()) {
			listmodelRoomChatPlayerList.clear();

			for(int i = 0; i < roomInfo.maxPlayers; i++) {
				listmodelRoomChatPlayerList.addElement("[" + (i + 1) + "]");
			}

			for(int i = 0; i < pList.size(); i++) {
				NetPlayerInfo pInfo = pList.get(i);

				if(pInfo.roomID == roomInfo.roomID) {
					// Name
					String name = getPlayerNameWithTripCode(pInfo);
					if(pInfo.uid == netPlayerClient.getPlayerUID()) name = "*" + getPlayerNameWithTripCode(pInfo);

					// Team
					if(pInfo.strTeam.length() > 0) {
						name = getPlayerNameWithTripCode(pInfo) + " - " + pInfo.strTeam;
						if(pInfo.uid == netPlayerClient.getPlayerUID()) name = "*" + getPlayerNameWithTripCode(pInfo) + " - " + pInfo.strTeam;
					}

					// Rating
					name += " |" + pInfo.rating[roomInfo.style] + "|";

					// Country code
					if(pInfo.strCountry.length() > 0) {
						name += " (" + pInfo.strCountry + ")";
					}

					/* XXX Hostname
					if(pInfo.strHost.length() > 0) {
						name += " {" + pInfo.strHost + "}";
					}
					*/

					// Status
					if(pInfo.playing) name += getUIText("RoomUserList_Playing");
					else if(pInfo.ready) name += getUIText("RoomUserList_Ready");

					if((pInfo.seatID >= 0) && (pInfo.seatID < roomInfo.maxPlayers)) {
						listmodelRoomChatPlayerList.set(pInfo.seatID, "[" + (pInfo.seatID + 1) + "] " + name);
					} else if(pInfo.queueID != -1) {
						listmodelRoomChatPlayerList.addElement((pInfo.queueID + 1) + ". " + name);
					} else {
						listmodelRoomChatPlayerList.addElement(name);
					}
				}
			}
		}
	}

	/**
	 * Being in the same roomPlayerUpdate the list
	 * @return Being in the same roomPlayerList
	 */
	public LinkedList<NetPlayerInfo> updateSameRoomPlayerInfoList() {
		LinkedList<NetPlayerInfo> pList = new LinkedList<NetPlayerInfo>(netPlayerClient.getPlayerInfoList());
		int roomID = netPlayerClient.getYourPlayerInfo().roomID;
		sameRoomPlayerInfoList.clear();

		for(NetPlayerInfo pInfo: pList) {
			if(pInfo.roomID == roomID) {
				sameRoomPlayerInfoList.add(pInfo);
			}
		}

		return sameRoomPlayerInfoList;
	}

	/**
	 * Being in the same roomPlayerReturns a list(The update does not)
	 * @return Being in the same roomPlayerList
	 */
	public LinkedList<NetPlayerInfo> getSameRoomPlayerInfoList() {
		return sameRoomPlayerInfoList;
	}

	/**
	 * Rule dataTransmission
	 */
	public void sendMyRuleDataToServer() {
		if(ruleOptPlayer == null) ruleOptPlayer = new RuleOptions();

		CustomProperties prop = new CustomProperties();
		ruleOptPlayer.writeProperty(prop, 0);
		String strRuleTemp = prop.encode("RuleData");
		String strRuleData = NetUtil.compressString(strRuleTemp);
		log.debug("RuleData uncompressed:" + strRuleTemp.length() + " compressed:" + strRuleData.length());

		// checkSam calculation
		Adler32 checksumObj = new Adler32();
		checksumObj.update(NetUtil.stringToBytes(strRuleData));
		long sChecksum = checksumObj.getValue();

		// Transmission
		netPlayerClient.send("ruledata\t" + sChecksum + "\t" + strRuleData + "\n");
	}

	/**
	 * To save the settings in the lobby
	 */
	public void saveConfig() {
		propConfig.setProperty("mainwindow.width", this.getSize().width);
		propConfig.setProperty("mainwindow.height", this.getSize().height);
		propConfig.setProperty("mainwindow.x", this.getLocation().x);
		propConfig.setProperty("mainwindow.y", this.getLocation().y);
		propConfig.setProperty("lobby.splitLobby.location", splitLobby.getDividerLocation());
		propConfig.setProperty("lobby.splitLobbyChat.location", splitLobbyChat.getDividerLocation());
		propConfig.setProperty("room.splitRoom.location", splitRoom.getDividerLocation());
		propConfig.setProperty("room.splitRoomChat.location", splitRoomChat.getDividerLocation());
		propConfig.setProperty("serverselect.txtfldPlayerName.text", txtfldPlayerName.getText());
		propConfig.setProperty("serverselect.txtfldPlayerTeam.text", txtfldPlayerTeam.getText());

		Object listboxServerListSelectedValue = listboxServerList.getSelectedValue();
		if((listboxServerListSelectedValue != null) && (listboxServerListSelectedValue instanceof String)) {
			propConfig.setProperty("serverselect.listboxServerList.value", (String)listboxServerListSelectedValue);
		} else {
			propConfig.setProperty("serverselect.listboxServerList.value", "");
		}

		TableColumnModel tm = tableRoomList.getColumnModel();
		propConfig.setProperty("tableRoomList.width.id", tm.getColumn(0).getWidth());
		propConfig.setProperty("tableRoomList.width.name", tm.getColumn(1).getWidth());
		propConfig.setProperty("tableRoomList.width.rated", tm.getColumn(2).getWidth());
		propConfig.setProperty("tableRoomList.width.rulename", tm.getColumn(3).getWidth());
		propConfig.setProperty("tableRoomList.width.modename", tm.getColumn(4).getWidth());
		propConfig.setProperty("tableRoomList.width.status", tm.getColumn(5).getWidth());
		propConfig.setProperty("tableRoomList.width.players", tm.getColumn(6).getWidth());
		propConfig.setProperty("tableRoomList.width.spectators", tm.getColumn(7).getWidth());

		tm = tableGameStat.getColumnModel();
		propConfig.setProperty("tableGameStat.width.rank", tm.getColumn(0).getWidth());
		propConfig.setProperty("tableGameStat.width.name", tm.getColumn(1).getWidth());
		propConfig.setProperty("tableGameStat.width.attack", tm.getColumn(2).getWidth());
		propConfig.setProperty("tableGameStat.width.apl", tm.getColumn(3).getWidth());
		propConfig.setProperty("tableGameStat.width.apm", tm.getColumn(4).getWidth());
		propConfig.setProperty("tableGameStat.width.lines", tm.getColumn(5).getWidth());
		propConfig.setProperty("tableGameStat.width.lpm", tm.getColumn(6).getWidth());
		propConfig.setProperty("tableGameStat.width.piece", tm.getColumn(7).getWidth());
		propConfig.setProperty("tableGameStat.width.pps", tm.getColumn(8).getWidth());
		propConfig.setProperty("tableGameStat.width.time", tm.getColumn(9).getWidth());
		propConfig.setProperty("tableGameStat.width.ko", tm.getColumn(10).getWidth());
		propConfig.setProperty("tableGameStat.width.wins", tm.getColumn(11).getWidth());
		propConfig.setProperty("tableGameStat.width.games", tm.getColumn(12).getWidth());

		tm = tableGameStat1P.getColumnModel();
		propConfig.setProperty("tableGameStat1P.width.description", tm.getColumn(0).getWidth());
		propConfig.setProperty("tableGameStat1P.width.value", tm.getColumn(1).getWidth());

		tm = tableMPRanking[0].getColumnModel();
		propConfig.setProperty("tableMPRanking.width.rank", tm.getColumn(0).getWidth());
		propConfig.setProperty("tableMPRanking.width.name", tm.getColumn(1).getWidth());
		propConfig.setProperty("tableMPRanking.width.rating", tm.getColumn(2).getWidth());
		propConfig.setProperty("tableMPRanking.width.play", tm.getColumn(3).getWidth());
		propConfig.setProperty("tableMPRanking.width.win", tm.getColumn(4).getWidth());

		if(backupRoomInfo != null) {
			propConfig.setProperty("createroom.defaultMaxPlayers", backupRoomInfo.maxPlayers);
			propConfig.setProperty("createroom.defaultAutoStartSeconds", backupRoomInfo.autoStartSeconds);
			propConfig.setProperty("createroom.defaultGravity", backupRoomInfo.gravity);
			propConfig.setProperty("createroom.defaultDenominator", backupRoomInfo.denominator);
			propConfig.setProperty("createroom.defaultARE", backupRoomInfo.are);
			propConfig.setProperty("createroom.defaultARELine", backupRoomInfo.areLine);
			propConfig.setProperty("createroom.defaultLineDelay", backupRoomInfo.lineDelay);
			propConfig.setProperty("createroom.defaultLockDelay", backupRoomInfo.lockDelay);
			propConfig.setProperty("createroom.defaultDAS", backupRoomInfo.das);
			propConfig.setProperty("createroom.defaultGarbagePercent", backupRoomInfo.garbagePercent);
			propConfig.setProperty("createroom.defaultTargetTimer", backupRoomInfo.targetTimer);
			propConfig.setProperty("createroom.defaultHurryupSeconds", backupRoomInfo.hurryupSeconds);
			propConfig.setProperty("createroom.defaultHurryupInterval", backupRoomInfo.hurryupInterval);
			propConfig.setProperty("createroom.defaultRuleLock", backupRoomInfo.ruleLock);
			propConfig.setProperty("createroom.defaultTSpinEnableType", backupRoomInfo.tspinEnableType);
			propConfig.setProperty("createroom.defaultSpinCheckType", backupRoomInfo.spinCheckType);
			propConfig.setProperty("createroom.defaultTSpinEnableEZ", backupRoomInfo.tspinEnableEZ);
			propConfig.setProperty("createroom.defaultB2B", backupRoomInfo.b2b);
			propConfig.setProperty("createroom.defaultCombo", backupRoomInfo.combo);
			propConfig.setProperty("createroom.defaultRensaBlock", backupRoomInfo.rensaBlock);
			propConfig.setProperty("createroom.defaultCounter", backupRoomInfo.counter);
			propConfig.setProperty("createroom.defaultBravo", backupRoomInfo.bravo);
			propConfig.setProperty("createroom.defaultReduceLineSend", backupRoomInfo.reduceLineSend);
			propConfig.setProperty("createroom.defaultGarbageChangePerAttack", backupRoomInfo.garbageChangePerAttack);
			propConfig.setProperty("createroom.defaultB2BChunk", backupRoomInfo.b2bChunk);
			propConfig.setProperty("createroom.defaultUseFractionalGarbage", backupRoomInfo.useFractionalGarbage);
			propConfig.setProperty("createroom.defaultIsTarget", backupRoomInfo.isTarget);
			propConfig.setProperty("createroom.defaultAutoStartTNET2", backupRoomInfo.autoStartTNET2);
			propConfig.setProperty("createroom.defaultDisableTimerAfterSomeoneCancelled", backupRoomInfo.disableTimerAfterSomeoneCancelled);
			propConfig.setProperty("createroom.defaultUseMap", backupRoomInfo.useMap);
			propConfig.setProperty("createroom.defaultMapSetID", (Integer)spinnerCreateRoomMapSetID.getValue());
		}

		Object listboxCreateRoom1PModeListSelectedValue = listboxCreateRoom1PModeList.getSelectedValue();
		if((listboxCreateRoom1PModeListSelectedValue != null) && (listboxCreateRoom1PModeListSelectedValue instanceof String)) {
			propConfig.setProperty("createroom1p.listboxCreateRoom1PModeList.value", (String)listboxCreateRoom1PModeListSelectedValue);
		} else {
			propConfig.setProperty("createroom1p.listboxCreateRoom1PModeList.value", "");
		}

		Object listboxCreateRoom1PRuleListSelectedValue = listboxCreateRoom1PRuleList.getSelectedValue();
		if((listboxCreateRoom1PRuleListSelectedValue != null) && (listboxCreateRoom1PRuleListSelectedValue instanceof String) &&
		   (listboxCreateRoom1PRuleList.getSelectedIndex() >= 1))
		{
			propConfig.setProperty("createroom1p.listboxCreateRoom1PRuleList.value", (String)listboxCreateRoom1PRuleListSelectedValue);
		} else {
			propConfig.setProperty("createroom1p.listboxCreateRoom1PRuleList.value", "");
		}

		propConfig.setProperty("createroom.defaultPresetID", (Integer)spinnerCreateRoomPresetID.getValue());

		try {
			FileOutputStream out = new FileOutputStream("config/setting/netlobby.cfg");
			propConfig.store(out, "NullpoMino NetLobby Config");
			out.close();
		} catch (IOException e) {
			log.warn("Failed to save netlobby config file", e);
		}
	}

	/**
	 * Save global config file
	 */
	public void saveGlobalConfig() {
		try {
			FileOutputStream out = new FileOutputStream("config/setting/global.cfg");
			propGlobal.store(out, "NullpoMino Global Config");
			out.close();
		} catch (IOException e) {
			log.warn("Failed to save global config file", e);
		}
	}

	/**
	 * End processing
	 */
	public void shutdown() {
		saveConfig();

		if(writerLobbyLog != null) {
			writerLobbyLog.flush();
			writerLobbyLog.close();
			writerLobbyLog = null;
		}
		if(writerRoomLog != null) {
			writerRoomLog.flush();
			writerRoomLog.close();
			writerRoomLog = null;
		}

		// Cut
		if(netPlayerClient != null) {
			if(netPlayerClient.isConnected()) {
				netPlayerClient.send("disconnect\n");
			}
			netPlayerClient.threadRunning = false;
			netPlayerClient.interrupt();
			netPlayerClient = null;
		}

		// ListenerCall
		if(listeners != null) {
			for(NetLobbyListener l: listeners) {
				l.netlobbyOnExit(this);
			}
			listeners = null;
		}
		if(netDummyMode != null) {
			netDummyMode.netlobbyOnExit(this);
			netDummyMode = null;
		}

		this.dispose();
	}

	/**
	 * Delete server buttonWhen processing is pressed
	 */
	public void serverSelectDeleteButtonClicked() {
		int index = listboxServerList.getSelectedIndex();
		if(index != -1) {
			String server = (String)listboxServerList.getSelectedValue();
			int answer = JOptionPane.showConfirmDialog(this,
													   getUIText("MessageBody_ServerDelete") + "\n" + server,
													   getUIText("MessageTitle_ServerDelete"),
													   JOptionPane.YES_NO_OPTION);
			if(answer == JOptionPane.YES_OPTION) {
				listmodelServerList.remove(index);
				saveListFromDefaultListModel(listmodelServerList, "config/setting/netlobby_serverlist.cfg");
			}
		}
	}

	/**
	 * Server connection buttonWhen processing is pressed
	 */
	public void serverSelectConnectButtonClicked() {
		int index = listboxServerList.getSelectedIndex();
		if(index != -1) {
			String strServer = (String)listboxServerList.getSelectedValue();
			int portSpliter = strServer.indexOf(":");
			if(portSpliter == -1) portSpliter = strServer.length();

			String strHost = strServer.substring(0, portSpliter);
			log.debug("Host:" + strHost);

			int port = NetPlayerClient.DEFAULT_PORT;
			try {
				String strPort = strServer.substring(portSpliter + 1, strServer.length());
				port = Integer.parseInt(strPort);
			} catch (Exception e2) {
				log.debug("Failed to get port number; Try to use default port");
			}
			log.debug("Port:" + port);

			netPlayerClient = new NetPlayerClient(strHost, port, txtfldPlayerName.getText(), txtfldPlayerTeam.getText().trim());
			netPlayerClient.setDaemon(true);
			netPlayerClient.addListener(this);
			netPlayerClient.start();

			txtpaneLobbyChatLog.setText("");
			setLobbyButtonsEnabled(0);
			tablemodelRoomList.setRowCount(0);

			changeCurrentScreenCard(SCREENCARD_LOBBY);
		}
	}

	/**
	 * Monitoring settings buttonWhen processing is pressed
	 */
	public void serverSelectSetObserverButtonClicked() {
		int index = listboxServerList.getSelectedIndex();
		if(index != -1) {
			String strServer = (String)listboxServerList.getSelectedValue();
			int portSpliter = strServer.indexOf(":");
			if(portSpliter == -1) portSpliter = strServer.length();

			String strHost = strServer.substring(0, portSpliter);
			log.debug("Host:" + strHost);

			int port = NetPlayerClient.DEFAULT_PORT;
			try {
				String strPort = strServer.substring(portSpliter + 1, strServer.length());
				port = Integer.parseInt(strPort);
			} catch (Exception e2) {
				log.debug("Failed to get port number; Try to use default port");
			}
			log.debug("Port:" + port);

			int answer = JOptionPane.showConfirmDialog(this,
					   getUIText("MessageBody_SetObserver") + "\n" + strServer,
					   getUIText("MessageTitle_SetObserver"),
					   JOptionPane.YES_NO_OPTION);

			if(answer == JOptionPane.YES_OPTION) {
				propObserver.setProperty("observer.enable", true);
				propObserver.setProperty("observer.host", strHost);
				propObserver.setProperty("observer.port", port);

				try {
					FileOutputStream out = new FileOutputStream("config/setting/netobserver.cfg");
					propObserver.store(out, "NullpoMino NetObserver Config");
					out.close();
				} catch (IOException e) {
					log.warn("Failed to save NetObserver config file", e);
				}
			}
		}
	}

	/**
	 * Get currenlty selected map set ID
	 * @return Map set ID
	 */
	public int getCurrentSelectedMapSetID() {
		if(spinnerCreateRoomMapSetID != null) {
			return (Integer)spinnerCreateRoomMapSetID.getValue();
		}
		return 0;
	}

	/**
	 * Send a chat message
	 * @param roomchat <code>true</code> if room chat
	 * @param strMsg Message to send
	 */
	public void sendChat(boolean roomchat, String strMsg) {
		String msg = strMsg;
		if(msg.startsWith("/team")) {
			msg = msg.replaceFirst("/team", "");
			msg = msg.trim();
			netPlayerClient.send("changeteam\t" + NetUtil.urlEncode(msg) + "\n");
		} else if(roomchat) {
			netPlayerClient.send("chat\t" + NetUtil.urlEncode(msg) + "\n");
		} else {
			netPlayerClient.send("lobbychat\t" + NetUtil.urlEncode(msg) + "\n");
		}
	}

	/**
	 * Creates NetRoomInfo from Create Room screen
	 * @return NetRoomInfo
	 */
	public NetRoomInfo exportRoomInfoFromCreateRoomScreen() {
		try {
			NetRoomInfo roomInfo = new NetRoomInfo();

			String roomName = txtfldCreateRoomName.getText();
			String modeName = (String)comboboxCreateRoomMode.getSelectedItem();
			Integer integerMaxPlayers = (Integer)spinnerCreateRoomMaxPlayers.getValue();
			Integer integerAutoStartSeconds = (Integer)spinnerCreateRoomAutoStartSeconds.getValue();
			Integer integerGravity = (Integer)spinnerCreateRoomGravity.getValue();
			Integer integerDenominator = (Integer)spinnerCreateRoomDenominator.getValue();
			Integer integerARE = (Integer)spinnerCreateRoomARE.getValue();
			Integer integerARELine = (Integer)spinnerCreateRoomARELine.getValue();
			Integer integerLineDelay = (Integer)spinnerCreateRoomLineDelay.getValue();
			Integer integerLockDelay = (Integer)spinnerCreateRoomLockDelay.getValue();
			Integer integerDAS = (Integer)spinnerCreateRoomDAS.getValue();
			Integer integerHurryupSeconds = (Integer)spinnerCreateRoomHurryupSeconds.getValue();
			Integer integerHurryupInterval = (Integer)spinnerCreateRoomHurryupInterval.getValue();
			boolean rulelock = chkboxCreateRoomRuleLock.isSelected();
			int tspinEnableType = comboboxCreateRoomTSpinEnableType.getSelectedIndex();
			int spinCheckType = comboboxCreateRoomSpinCheckType.getSelectedIndex();
			boolean tspinEnableEZ = chkboxCreateRoomTSpinEnableEZ.isSelected();
			boolean b2b = chkboxCreateRoomB2B.isSelected();
			boolean combo = chkboxCreateRoomCombo.isSelected();
			boolean rensaBlock = chkboxCreateRoomRensaBlock.isSelected();
			boolean counter = chkboxCreateRoomCounter.isSelected();
			boolean bravo = chkboxCreateRoomBravo.isSelected();
			boolean reduceLineSend = chkboxCreateRoomReduceLineSend.isSelected();
			boolean autoStartTNET2 = chkboxCreateRoomAutoStartTNET2.isSelected();
			boolean disableTimerAfterSomeoneCancelled = chkboxCreateRoomDisableTimerAfterSomeoneCancelled.isSelected();
			boolean useMap = chkboxCreateRoomUseMap.isSelected();
			boolean useFractionalGarbage = chkboxCreateRoomUseFractionalGarbage.isSelected();
			boolean garbageChangePerAttack = chkboxCreateRoomGarbageChangePerAttack.isSelected();
			boolean divideChangeRateByPlayers = chkboxCreateRoomDivideChangeRateByPlayers.isSelected();
			Integer integerGarbagePercent = (Integer)spinnerCreateRoomGarbagePercent.getValue();
			boolean b2bChunk = chkboxCreateRoomB2BChunk.isSelected();
			boolean isTarget = chkboxCreateRoomIsTarget.isSelected();
			Integer integerTargetTimer = (Integer)spinnerCreateRoomTargetTimer.getValue();

			roomInfo.strName = roomName;
			roomInfo.strMode = modeName;
			roomInfo.maxPlayers = integerMaxPlayers;
			roomInfo.autoStartSeconds = integerAutoStartSeconds;
			roomInfo.gravity = integerGravity;
			roomInfo.denominator = integerDenominator;
			roomInfo.are = integerARE;
			roomInfo.areLine = integerARELine;
			roomInfo.lineDelay = integerLineDelay;
			roomInfo.lockDelay = integerLockDelay;
			roomInfo.das = integerDAS;
			roomInfo.hurryupSeconds = integerHurryupSeconds;
			roomInfo.hurryupInterval = integerHurryupInterval;
			roomInfo.ruleLock = rulelock;
			roomInfo.tspinEnableType = tspinEnableType;
			roomInfo.spinCheckType = spinCheckType;
			roomInfo.tspinEnableEZ = tspinEnableEZ;
			roomInfo.b2b = b2b;
			roomInfo.combo = combo;
			roomInfo.rensaBlock = rensaBlock;
			roomInfo.counter = counter;
			roomInfo.bravo = bravo;
			roomInfo.reduceLineSend = reduceLineSend;
			roomInfo.autoStartTNET2 = autoStartTNET2;
			roomInfo.disableTimerAfterSomeoneCancelled = disableTimerAfterSomeoneCancelled;
			roomInfo.useMap = useMap;
			roomInfo.useFractionalGarbage = useFractionalGarbage;
			roomInfo.garbageChangePerAttack = garbageChangePerAttack;
			roomInfo.divideChangeRateByPlayers = divideChangeRateByPlayers;
			roomInfo.garbagePercent = integerGarbagePercent;
			roomInfo.b2bChunk = b2bChunk;
			roomInfo.isTarget = isTarget;
			roomInfo.targetTimer = integerTargetTimer;

			return roomInfo;
		} catch (Exception e) {
			log.error("Exception on exportRoomInfoFromCreateRoomScreen", e);
		}
		return null;
	}

	/**
	 * Import NetRoomInfo to Create Room screen
	 * @param r NetRoomInfo
	 */
	public void importRoomInfoToCreateRoomScreen(NetRoomInfo r) {
		if(r != null) {
			txtfldCreateRoomName.setText(r.strName);
			comboboxCreateRoomMode.setSelectedIndex(0);
			if(r.strMode.length() > 0) {
				comboboxCreateRoomMode.setSelectedItem(r.strMode);
			}
			spinnerCreateRoomMaxPlayers.setValue(r.maxPlayers);
			spinnerCreateRoomAutoStartSeconds.setValue(r.autoStartSeconds);
			spinnerCreateRoomGravity.setValue(r.gravity);
			spinnerCreateRoomDenominator.setValue(r.denominator);
			spinnerCreateRoomARE.setValue(r.are);
			spinnerCreateRoomARELine.setValue(r.areLine);
			spinnerCreateRoomLineDelay.setValue(r.lineDelay);
			spinnerCreateRoomLockDelay.setValue(r.lockDelay);
			spinnerCreateRoomDAS.setValue(r.das);
			spinnerCreateRoomHurryupSeconds.setValue(r.hurryupSeconds);
			spinnerCreateRoomHurryupInterval.setValue(r.hurryupInterval);
			spinnerCreateRoomGarbagePercent.setValue(r.garbagePercent);
			spinnerCreateRoomTargetTimer.setValue(r.targetTimer);
			chkboxCreateRoomUseMap.setSelected(r.useMap);
			chkboxCreateRoomRuleLock.setSelected(r.ruleLock);
			comboboxCreateRoomTSpinEnableType.setSelectedIndex(r.tspinEnableType);
			comboboxCreateRoomSpinCheckType.setSelectedIndex(r.spinCheckType);
			chkboxCreateRoomTSpinEnableEZ.setSelected(r.tspinEnableEZ);
			chkboxCreateRoomB2B.setSelected(r.b2b);
			chkboxCreateRoomCombo.setSelected(r.combo);
			chkboxCreateRoomRensaBlock.setSelected(r.rensaBlock);
			chkboxCreateRoomCounter.setSelected(r.counter);
			chkboxCreateRoomBravo.setSelected(r.bravo);
			chkboxCreateRoomReduceLineSend.setSelected(r.reduceLineSend);
			chkboxCreateRoomGarbageChangePerAttack.setSelected(r.garbageChangePerAttack);
			chkboxCreateRoomDivideChangeRateByPlayers.setSelected(r.divideChangeRateByPlayers);
			chkboxCreateRoomB2BChunk.setSelected(r.b2bChunk);
			chkboxCreateRoomUseFractionalGarbage.setSelected(r.useFractionalGarbage);
			chkboxCreateRoomIsTarget.setSelected(r.isTarget);
			chkboxCreateRoomAutoStartTNET2.setSelected(r.autoStartTNET2);
			chkboxCreateRoomDisableTimerAfterSomeoneCancelled.setSelected(r.disableTimerAfterSomeoneCancelled);
		}
	}

	/**
	 * Get rule file list (for rule change screen)
	 * @return Rule file list. null if directory doesn't exist.
	 */
	public String[] getRuleFileList() {
		File dir = new File("config/rule");

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir1, String name) {
				return name.endsWith(".rul");
			}
		};

		String[] list = dir.list(filter);

		if(!System.getProperty("os.name").startsWith("Windows")) {
			// Sort if not windows
			Arrays.sort(list);
		}

		return list;
	}

	/**
	 * Create rule entries (for rule change screen)
	 * @param filelist Rule file list
	 */
	public void createRuleEntries(String[] filelist) {
		ruleEntries = new LinkedList<RuleEntry>();

		for(int i = 0; i < filelist.length; i++) {
			RuleEntry entry = new RuleEntry();

			File file = new File("config/rule/" + filelist[i]);
			entry.filename = filelist[i];
			entry.filepath = file.getPath();

			CustomProperties prop = new CustomProperties();
			try {
				FileInputStream in = new FileInputStream("config/rule/" + filelist[i]);
				prop.load(in);
				in.close();
				entry.rulename = prop.getProperty("0.ruleopt.strRuleName", "");
				entry.style = prop.getProperty("0.ruleopt.style", 0);
			} catch (Exception e) {
				entry.rulename = "";
				entry.style = -1;
			}

			ruleEntries.add(entry);
		}
	}

	/**
	 * Get subset of rule entries (for rule change screen)
	 * @param currentStyle Current style
	 * @return Subset of rule entries
	 */
	public LinkedList<RuleEntry> getSubsetEntries(int currentStyle) {
		LinkedList<RuleEntry> subEntries = new LinkedList<RuleEntry>();
		for(int i = 0; i < ruleEntries.size(); i++) {
			if(ruleEntries.get(i).style == currentStyle) {
				subEntries.add(ruleEntries.get(i));
			}
		}
		return subEntries;
	}

	/**
	 * Get rule name + file name list as String[] (for rule change screen)
	 * @param currentStyle Current style
	 * @return Rule name + file name list
	 */
	public String[] extractRuleListFromRuleEntries(int currentStyle) {
		LinkedList<RuleEntry> subEntries = getSubsetEntries(currentStyle);

		String[] result = new String[subEntries.size()];
		for(int i = 0; i < subEntries.size(); i++) {
			RuleEntry entry = subEntries.get(i);
			result[i] = entry.rulename + " (" + entry.filename + ")";
		}

		return result;
	}

	/**
	 * Enter rule change screen
	 */
	public void enterRuleChangeScreen() {
		// Set rule selections
		String[] strCurrentFileName = new String[GameEngine.MAX_GAMESTYLE];

		for(int i = 0; i < GameEngine.MAX_GAMESTYLE; i++) {
			if(i == 0) {
				strCurrentFileName[i] = propGlobal.getProperty(0 + ".rulefile", "");
			} else {
				strCurrentFileName[i] = propGlobal.getProperty(0 + ".rulefile." + i, "");
			}

			LinkedList<RuleEntry> subEntries = getSubsetEntries(i);

			for(int j = 0; j < subEntries.size(); j++) {
				if(subEntries.get(j).filename.equals(strCurrentFileName[i])) {
					listboxRuleChangeRuleList[i].setSelectedIndex(j);
				}
			}
		}

		// Tuning
		int owRotateButtonDefaultRight = propGlobal.getProperty(0 + ".tuning.owRotateButtonDefaultRight", -1) + 1;
		comboboxTuningRotateButtonDefaultRight.setSelectedIndex(owRotateButtonDefaultRight);
		int owMoveDiagonal = propGlobal.getProperty(0 + ".tuning.owMoveDiagonal", -1) + 1;
		comboboxTuningMoveDiagonal.setSelectedIndex(owMoveDiagonal);
		int owBlockShowOutlineOnly = propGlobal.getProperty(0 + ".tuning.owBlockShowOutlineOnly", -1) + 1;
		comboboxTuningBlockShowOutlineOnly.setSelectedIndex(owBlockShowOutlineOnly);
		int owSkin = propGlobal.getProperty(0 + ".tuning.owSkin", -1) + 1;
		comboboxTuningSkin.setSelectedIndex(owSkin);
		int owBlockOutlineType = propGlobal.getProperty(0 + ".tuning.owBlockOutlineType", -1) + 1;
		comboboxTuningBlockOutlineType.setSelectedIndex(owBlockOutlineType);

		txtfldTuningMinDAS.setText(propGlobal.getProperty(0 + ".tuning.owMinDAS", "-1"));
		txtfldTuningMaxDAS.setText(propGlobal.getProperty(0 + ".tuning.owMaxDAS", "-1"));
		txtfldTuningDasDelay.setText(propGlobal.getProperty(0 + ".tuning.owDasDelay", "-1"));
		chkboxTuningReverseUpDown.setSelected(propGlobal.getProperty(0 + ".tuning.owReverseUpDown", false));

		// Change screen
		changeCurrentScreenCard(SCREENCARD_RULECHANGE);
	}

	/*
	 * Menu What Happens at Runtime
	 */
	public void actionPerformed(ActionEvent e) {
		//addSystemChatLog(getCurrentChatLogTextPane(), e.getActionCommand(), Color.magenta);

		// Add Server
		if(e.getActionCommand() == "ServerSelect_ServerAdd") {
			changeCurrentScreenCard(SCREENCARD_SERVERADD);
		}
		// Delete server
		if(e.getActionCommand() == "ServerSelect_ServerDelete") {
			serverSelectDeleteButtonClicked();
		}
		// Server connection
		if(e.getActionCommand() == "ServerSelect_Connect") {
			serverSelectConnectButtonClicked();
		}
		// Monitoring settings
		if(e.getActionCommand() == "ServerSelect_SetObserver") {
			serverSelectSetObserverButtonClicked();
		}
		// Unmonitor
		if(e.getActionCommand() == "ServerSelect_UnsetObserver") {
			if(propObserver.getProperty("observer.enable", false) == true) {
				String strCurrentHost = propObserver.getProperty("observer.host", "");
				int currentPort = propObserver.getProperty("observer.port", 0);
				String strMessageBox = String.format(getUIText("MessageBody_UnsetObserver"), strCurrentHost, currentPort);

				int answer = JOptionPane.showConfirmDialog(this, strMessageBox, getUIText("MessageTitle_UnsetObserver"), JOptionPane.YES_NO_OPTION);

				if(answer == JOptionPane.YES_OPTION) {
					propObserver.setProperty("observer.enable", false);
					try {
						FileOutputStream out = new FileOutputStream("config/setting/netobserver.cfg");
						propObserver.store(out, "NullpoMino NetObserver Config");
						out.close();
					} catch (IOException e2) {
						log.warn("Failed to save NetObserver config file", e2);
					}
				}
			}
		}
		// End
		if(e.getActionCommand() == "ServerSelect_Exit") {
			shutdown();
		}
		// Quick Start
		if(e.getActionCommand() == "Lobby_QuickStart") {
			// TODO:Quick Start
		}
		// Create Room 1P
		if(e.getActionCommand() == "Lobby_RoomCreate1P") {
			currentViewDetailRoomID = -1;
			setCreateRoom1PUIType(false, null);
			changeCurrentScreenCard(SCREENCARD_CREATEROOM1P);
		}
		// Create Room Multiplayer
		if(e.getActionCommand() == "Lobby_RoomCreate") {
			currentViewDetailRoomID = -1;
			// setCreateRoomUIType(false, null);
			changeCurrentScreenCard(SCREENCARD_CREATERATED_WAITING);
			netPlayerClient.send("getpresets\n");
		}
		// Lobby Options
		if(e.getActionCommand() == "Lobby_Options") {
			popupLobbyOptions.show(btnRoomListOptions, 0, 0);
		}
		// Rule Change
		if(e.getActionCommand() == "Lobby_RuleChange") {
			enterRuleChangeScreen();
		}
		// Change team(Lobby screen)
		if(e.getActionCommand() == "Lobby_TeamChange") {
			if((netPlayerClient != null) && (netPlayerClient.isConnected())) {
				txtfldRoomListTeam.setText(netPlayerClient.getYourPlayerInfo().strTeam);
				roomListTopBarCardLayout.next(subpanelRoomListTopBar);
			}
		}
		// Cut
		if(e.getActionCommand() == "Lobby_Disconnect") {
			if((netPlayerClient != null) && (netPlayerClient.isConnected())) {
				netPlayerClient.send("disconnect\n");
				netPlayerClient.threadRunning = false;
				netPlayerClient.interrupt();
				netPlayerClient = null;
			}
			tabLobbyAndRoom.setSelectedIndex(0);
			tabLobbyAndRoom.setEnabledAt(1, false);
			tabLobbyAndRoom.setTitleAt(1, getUIText("Lobby_Tab_NoRoom"));
			setLobbyButtonsEnabled(1);
			setTitle(getUIText("Title_NetLobby"));
			changeCurrentScreenCard(SCREENCARD_SERVERSELECT);
		}
		// Multiplayer Leaderboard
		if((e.getActionCommand() == "Lobby_Ranking") || (e.getActionCommand() == "Room_Ranking")) {
			if((netPlayerClient != null) && (netPlayerClient.isConnected())) {
				tablemodelMPRanking[0].setRowCount(0);
				netPlayerClient.send("mpranking\t0\n");
				changeCurrentScreenCard(SCREENCARD_MPRANKING);
			}
		}
		// Submit chat
		if((e.getActionCommand() == "Lobby_ChatSend") || (e.getActionCommand() == "Room_ChatSend")) {
			if((txtfldLobbyChatInput.getText().length() > 0) && (netPlayerClient != null) && netPlayerClient.isConnected()) {
				sendChat(false, txtfldLobbyChatInput.getText());
				txtfldLobbyChatInput.setText("");
			}

			if((netPlayerClient != null) && netPlayerClient.isConnected()) {
				if(tabLobbyAndRoom.getSelectedIndex() == 0) {
					if(txtfldLobbyChatInput.getText().length() > 0) {
						sendChat(false, txtfldLobbyChatInput.getText());
						txtfldLobbyChatInput.setText("");
					}
				} else {
					if(txtfldRoomChatInput.getText().length() > 0) {
						sendChat(true, txtfldRoomChatInput.getText());
						txtfldRoomChatInput.setText("");
					}
				}
			}
		}
		// Change teamOK(Lobby screen)
		if(e.getActionCommand() == "Lobby_TeamChange_OK") {
			if((netPlayerClient != null) && (netPlayerClient.isConnected())) {
				netPlayerClient.send("changeteam\t" + NetUtil.urlEncode(txtfldRoomListTeam.getText()) + "\n");
				roomListTopBarCardLayout.first(subpanelRoomListTopBar);
			}
		}
		// Change teamCancel(Lobby screen)
		if(e.getActionCommand() == "Lobby_TeamChange_Cancel") {
			roomListTopBarCardLayout.first(subpanelRoomListTopBar);
		}
		// Withdrawal button
		if(e.getActionCommand() == "Room_Leave") {
			if((netPlayerClient != null) && (netPlayerClient.isConnected())) {
				netPlayerClient.send("roomjoin\t-1\tfalse\n");
			}

			tablemodelGameStat.setRowCount(0);
			tablemodelGameStat1P.setRowCount(0);

			tabLobbyAndRoom.setSelectedIndex(0);
			tabLobbyAndRoom.setEnabledAt(1, false);
			tabLobbyAndRoom.setTitleAt(1, getUIText("Lobby_Tab_NoRoom"));

			changeCurrentScreenCard(SCREENCARD_LOBBY);

			// Listener call
			for(NetLobbyListener l: listeners) {
				l.netlobbyOnRoomLeave(this, netPlayerClient);
			}
			if(netDummyMode != null) netDummyMode.netlobbyOnRoomLeave(this, netPlayerClient);
		}
		// Participation in a war button
		if(e.getActionCommand() == "Room_Join") {
			netPlayerClient.send("changestatus\tfalse\n");
			btnRoomButtonsJoin.setEnabled(false);
		}
		// Withdrawal(Watching only) button
		if(e.getActionCommand() == "Room_SitOut") {
			netPlayerClient.send("changestatus\ttrue\n");
			btnRoomButtonsSitOut.setEnabled(false);
		}
		// Change team(Room screen)
		if(e.getActionCommand() == "Room_TeamChange") {
			if((netPlayerClient != null) && (netPlayerClient.isConnected())) {
				txtfldRoomTeam.setText(netPlayerClient.getYourPlayerInfo().strTeam);
				roomTopBarCardLayout.next(subpanelRoomTopBar);
			}
		}
		// Change teamOK(Room screen)
		if(e.getActionCommand() == "Room_TeamChange_OK") {
			if((netPlayerClient != null) && (netPlayerClient.isConnected())) {
				netPlayerClient.send("changeteam\t" + NetUtil.urlEncode(txtfldRoomTeam.getText()) + "\n");
				roomTopBarCardLayout.first(subpanelRoomTopBar);
			}
		}
		// Change teamCancel(Room screen)
		if(e.getActionCommand() == "Room_TeamChange_Cancel") {
			roomTopBarCardLayout.first(subpanelRoomTopBar);
		}
		// Confirmation rule(Room screen)
		if(e.getActionCommand() == "Room_ViewSetting") {
			viewRoomDetail(netPlayerClient.getYourPlayerInfo().roomID);
		}
		// In the Add Server screenOK button
		if(e.getActionCommand() == "ServerAdd_OK") {
			if(txtfldServerAddHost.getText().length() > 0) {
				listmodelServerList.addElement(txtfldServerAddHost.getText());
				saveListFromDefaultListModel(listmodelServerList, "config/setting/netlobby_serverlist.cfg");
				txtfldServerAddHost.setText("");
			}
			changeCurrentScreenCard(SCREENCARD_SERVERSELECT);
		}
		// In the Add Server screenCancel button
		if(e.getActionCommand() == "ServerAdd_Cancel") {
			txtfldServerAddHost.setText("");
			changeCurrentScreenCard(SCREENCARD_SERVERSELECT);
		}
		// Create rated cancel from waiting card
		if(e.getActionCommand() == "CreateRated_Waiting_Cancel") {
			currentViewDetailRoomID = -1;
			changeCurrentScreenCard(SCREENCARD_LOBBY);
		}
		// Create rated OK
		if(e.getActionCommand() == "CreateRated_OK") {
			try {
				int presetIndex = comboboxCreateRatedPresets.getSelectedIndex();
				NetRoomInfo r = presets.get(presetIndex);
				r.strName = txtfldCreateRatedName.getText();
				backupRoomInfo = r;

				String msg = "ratedroomcreate\t"+NetUtil.urlEncode(r.strName)+"\t"
				+spinnerCreateRatedMaxPlayers.getValue()+"\t"+presetIndex+"\t"
				+NetUtil.urlEncode("NET-VS-BATTLE")+"\n";

				txtpaneRoomChatLog.setText("");
				setRoomButtonsEnabled(false);
				tabLobbyAndRoom.setEnabledAt(1, true);
				tabLobbyAndRoom.setSelectedIndex(1);
				changeCurrentScreenCard(SCREENCARD_LOBBY);

				netPlayerClient.send(msg);
			} catch (Exception e2) {
				log.error("Error on CreateRated_OK", e2);
			}
		}
		// Create rated - go to custom settings
		if(e.getActionCommand() == "CreateRated_Custom") {
			// Load preset into field
			NetRoomInfo r = presets.get(comboboxCreateRatedPresets.getSelectedIndex());
			setCreateRoomUIType(false, null);
			importRoomInfoToCreateRoomScreen(r);
			// Copy name and number of players
			txtfldCreateRoomName.setText(txtfldCreateRatedName.getText());
			spinnerCreateRoomMaxPlayers.setValue(spinnerCreateRatedMaxPlayers.getValue());
			// Change screen card
			changeCurrentScreenCard(SCREENCARD_CREATEROOM);
		}
		// Create rated cancel
		if(e.getActionCommand() == "CreateRated_Cancel") {
			currentViewDetailRoomID = -1;
			changeCurrentScreenCard(SCREENCARD_LOBBY);
		}
		// Room-created screensOK button
		if(e.getActionCommand() == "CreateRoom_OK") {
			try {
				NetRoomInfo r = exportRoomInfoFromCreateRoomScreen();
				backupRoomInfo = r;

				String msg;
				msg = "roomcreate\t" + NetUtil.urlEncode(r.strName) + "\t";
				msg += NetUtil.urlEncode(r.exportString()) + "\t";
				msg += NetUtil.urlEncode(r.strMode) + "\t";

				// Map send
				if(r.useMap) {
					int setID = getCurrentSelectedMapSetID();
					log.debug("MapSetID:" + setID);

					mapList.clear();
					CustomProperties propMap = new CustomProperties();
					try {
						FileInputStream in = new FileInputStream("config/map/vsbattle/" + setID + ".map");
						propMap.load(in);
						in.close();
					} catch (IOException e2) {
						log.error("Map set " + setID + " not found", e2);
					}

					int maxMap = propMap.getProperty("map.maxMapNumber", 0);
					log.debug("Number of maps:" + maxMap);

					String strMap = "";

					for(int i = 0; i < maxMap; i++) {
						String strMapTemp = propMap.getProperty("map." + i, "");
						mapList.add(strMapTemp);
						strMap += strMapTemp;
						if(i < maxMap - 1) strMap += "\t";
					}

					String strCompressed = NetUtil.compressString(strMap);
					log.debug("Map uncompressed:" + strMap.length() + " compressed:" + strCompressed.length());

					msg += strCompressed;
				}

				msg += "\n";

				txtpaneRoomChatLog.setText("");
				setRoomButtonsEnabled(false);
				tabLobbyAndRoom.setEnabledAt(1, true);
				tabLobbyAndRoom.setSelectedIndex(1);
				changeCurrentScreenCard(SCREENCARD_LOBBY);

				netPlayerClient.send(msg);
			} catch (Exception e2) {
				log.error("Error on CreateRoom_OK", e2);
			}
		}
		// Save Preset (Create Room)
		if(e.getActionCommand() == "CreateRoom_PresetSave") {
			NetRoomInfo r = exportRoomInfoFromCreateRoomScreen();
			Integer id = (Integer)spinnerCreateRoomPresetID.getValue();
			propConfig.setProperty("0.preset." + id, NetUtil.compressString(r.exportString()));
		}
		// Load Preset (Create Room)
		if(e.getActionCommand() == "CreateRoom_PresetLoad") {
			Integer id = (Integer)spinnerCreateRoomPresetID.getValue();
			String strPresetC = propConfig.getProperty("0.preset." + id);
			if(strPresetC != null) {
				String strPreset = NetUtil.decompressString(strPresetC);
				NetRoomInfo r = new NetRoomInfo(strPreset);
				importRoomInfoToCreateRoomScreen(r);
			}
		}
		// Preset code export (Create Room)
		if(e.getActionCommand() == "CreateRoom_PresetCodeExport") {
			NetRoomInfo r = exportRoomInfoFromCreateRoomScreen();
			if(r == null) {
				txtfldCreateRoomPresetCode.setText("");
			} else {
				txtfldCreateRoomPresetCode.setText(NetUtil.compressString(r.exportString()));
			}
		}
		// Preset code import (Create Room)
		if(e.getActionCommand() == "CreateRoom_PresetCodeImport") {
			try {
				String strPresetCode = txtfldCreateRoomPresetCode.getText();
				strPresetCode = strPresetCode.replaceAll("[^a-zA-Z0-9+/=]", "");
				if(strPresetCode.length() > 0) {
					String strPresetCodeD = NetUtil.decompressString(strPresetCode);
					NetRoomInfo r = new NetRoomInfo(strPresetCodeD);
					importRoomInfoToCreateRoomScreen(r);
				}
			} catch (Exception e2) {
				log.error("Failed to import preset code", e2);
			}
		}
		// Participated in the creation screen room button
		if(e.getActionCommand() == "CreateRoom_Join") {
			joinRoom(currentViewDetailRoomID, false);
		}
		// Watch-created screens Room button
		if(e.getActionCommand() == "CreateRoom_Watch") {
			joinRoom(currentViewDetailRoomID, true);
		}
		// Room-created screensCancel button
		if(e.getActionCommand() == "CreateRoom_Cancel") {
			currentViewDetailRoomID = -1;
			changeCurrentScreenCard(SCREENCARD_LOBBY);
		}
		// OK button (Create Room 1P)
		if(e.getActionCommand() == "CreateRoom1P_OK") {
			//singleroomcreate\t[roomName]\t[mode]
			try {
				if(currentViewDetailRoomID != -1) {
					joinRoom(currentViewDetailRoomID, true);
				} else if(listboxCreateRoom1PModeList.getSelectedIndex() != -1) {
					String strMode = (String)listboxCreateRoom1PModeList.getSelectedValue();
					String strRule = "";
					if(listboxCreateRoom1PRuleList.getSelectedIndex() >= 1) {
						strRule = (String)listboxCreateRoom1PRuleList.getSelectedValue();
					}

					txtpaneRoomChatLog.setText("");
					setRoomButtonsEnabled(false);
					tabLobbyAndRoom.setEnabledAt(1, true);
					tabLobbyAndRoom.setSelectedIndex(1);
					changeCurrentScreenCard(SCREENCARD_LOBBY);

					netPlayerClient.send("singleroomcreate\t" + "\t" + NetUtil.urlEncode(strMode) + "\t" + NetUtil.urlEncode(strRule) + "\n");
				}
			} catch (Exception e2) {
				log.error("Error on CreateRoom1P_OK", e2);
			}
		}
		// Cancel button (Create Room 1P)
		if(e.getActionCommand() == "CreateRoom1P_Cancel") {
			currentViewDetailRoomID = -1;
			changeCurrentScreenCard(SCREENCARD_LOBBY);
		}
		// OK button (MPRanking)
		if(e.getActionCommand() == "MPRanking_OK") {
			changeCurrentScreenCard(SCREENCARD_LOBBY);
		}
		// Cancel button (Rule change)
		if(e.getActionCommand() == "RuleChange_Cancel") {
			changeCurrentScreenCard(SCREENCARD_LOBBY);
		}
		// OK button (Rule change)
		if(e.getActionCommand() == "RuleChange_OK") {
			// Set rules
			String strPrevTetrominoRuleFilename = propGlobal.getProperty(0 + ".rule", "");

			for(int i = 0; i < GameEngine.MAX_GAMESTYLE; i++) {
				int id = listboxRuleChangeRuleList[i].getSelectedIndex();
				LinkedList<RuleEntry> subEntries = getSubsetEntries(i);
				RuleEntry entry = null;
				if (id >= 0) { entry = subEntries.get(id); }


				if(i == 0) {
					if(id >= 0) {
						propGlobal.setProperty(0 + ".rule", entry.filepath);
						propGlobal.setProperty(0 + ".rulefile", entry.filename);
						propGlobal.setProperty(0 + ".rulename", entry.rulename);
					} else {
						propGlobal.setProperty(0 + ".rule", "");
						propGlobal.setProperty(0 + ".rulefile", "");
						propGlobal.setProperty(0 + ".rulename", "");
					}
				} else {
					if(id >= 0) {
						propGlobal.setProperty(0 + ".rule." + i, entry.filepath);
						propGlobal.setProperty(0 + ".rulefile." + i, entry.filename);
						propGlobal.setProperty(0 + ".rulename." + i, entry.rulename);
					} else {
						propGlobal.setProperty(0 + ".rule." + i, "");
						propGlobal.setProperty(0 + ".rulefile." + i, "");
						propGlobal.setProperty(0 + ".rulename." + i, "");
					}
				}
			}

			// Tuning
			int owRotateButtonDefaultRight = comboboxTuningRotateButtonDefaultRight.getSelectedIndex() - 1;
			propGlobal.setProperty(0 + ".tuning.owRotateButtonDefaultRight", owRotateButtonDefaultRight);

			int owMoveDiagonal = comboboxTuningMoveDiagonal.getSelectedIndex() - 1;
			propGlobal.setProperty(0 + ".tuning.owMoveDiagonal", owMoveDiagonal);

			int owBlockShowOutlineOnly = comboboxTuningBlockShowOutlineOnly.getSelectedIndex() - 1;
			propGlobal.setProperty(0 + ".tuning.owBlockShowOutlineOnly", owBlockShowOutlineOnly);

			int owSkin = comboboxTuningSkin.getSelectedIndex() - 1;
			propGlobal.setProperty(0 + ".tuning.owSkin", owSkin);

			int owBlockOutlineType = comboboxTuningBlockOutlineType.getSelectedIndex() - 1;
			propGlobal.setProperty(0 + ".tuning.owBlockOutlineType", owBlockOutlineType);

			int owMinDAS = getIntTextField(-1, txtfldTuningMinDAS);
			propGlobal.setProperty(0 + ".tuning.owMinDAS", owMinDAS);
			int owMaxDAS = getIntTextField(-1, txtfldTuningMaxDAS);
			propGlobal.setProperty(0 + ".tuning.owMaxDAS", owMaxDAS);
			int owDasDelay = getIntTextField(-1, txtfldTuningDasDelay);
			propGlobal.setProperty(0 + ".tuning.owDasDelay", owDasDelay);
			boolean owReverseUpDown = chkboxTuningReverseUpDown.isSelected();
			propGlobal.setProperty(0 + ".tuning.owReverseUpDown", owReverseUpDown);

			// Save
			saveGlobalConfig();

			// Load rule
			String strFileName = propGlobal.getProperty(0 + ".rule", "");
			if(!strPrevTetrominoRuleFilename.equals(strFileName)) {
				CustomProperties propRule = new CustomProperties();
				try {
					FileInputStream in = new FileInputStream(strFileName);
					propRule.load(in);
					in.close();
				} catch (Exception e2) {}
				ruleOptPlayer = new RuleOptions();
				ruleOptPlayer.readProperty(propRule, 0);

				// Send rule
				if((netPlayerClient != null) && (netPlayerClient.isConnected())) {
					sendMyRuleDataToServer();
				}
			}

			changeCurrentScreenCard(SCREENCARD_LOBBY);
		}
	}

	/*
	 * Message reception
	 */
	public void netOnMessage(NetBaseClient client, String[] message) throws IOException {
		//addSystemChatLog(getCurrentChatLogTextPane(), message[0], Color.green);

		// Connection completion
		if(message[0].equals("welcome")) {
			//welcome\t[VERSION]\t[PLAYERS]
			// Chat logFile creation
			if(writerLobbyLog == null) {
				try {
					GregorianCalendar currentTime = new GregorianCalendar();
					int month = currentTime.get(Calendar.MONTH) + 1;
					String filename = String.format(
							"log/lobby_%04d_%02d_%02d_%02d_%02d_%02d.txt",
							currentTime.get(Calendar.YEAR), month, currentTime.get(Calendar.DATE), currentTime.get(Calendar.HOUR_OF_DAY),
							currentTime.get(Calendar.MINUTE), currentTime.get(Calendar.SECOND)
					);
					writerLobbyLog = new PrintWriter(filename);
				} catch (Exception e) {
					log.warn("Failed to create lobby log file", e);
				}
			}
			if(writerRoomLog == null) {
				try {
					GregorianCalendar currentTime = new GregorianCalendar();
					int month = currentTime.get(Calendar.MONTH) + 1;
					String filename = String.format(
							"log/room_%04d_%02d_%02d_%02d_%02d_%02d.txt",
							currentTime.get(Calendar.YEAR), month, currentTime.get(Calendar.DATE), currentTime.get(Calendar.HOUR_OF_DAY),
							currentTime.get(Calendar.MINUTE), currentTime.get(Calendar.SECOND)
					);
					writerRoomLog = new PrintWriter(filename);
				} catch (Exception e) {
					log.warn("Failed to create room log file", e);
				}
			}

			String strTemp = String.format(getUIText("SysMsg_ServerConnected"), netPlayerClient.getHost(), netPlayerClient.getPort());
			addSystemChatLogLater(txtpaneLobbyChatLog, strTemp, Color.blue);

			addSystemChatLogLater(txtpaneLobbyChatLog, getUIText("SysMsg_ServerVersion") + message[1], Color.blue);
			addSystemChatLogLater(txtpaneLobbyChatLog, getUIText("SysMsg_NumberOfPlayers") + message[2], Color.blue);
		}
		// Successful login
		if(message[0].equals("loginsuccess")) {
			addSystemChatLogLater(txtpaneLobbyChatLog, getUIText("SysMsg_LoginOK"), Color.blue);
			addSystemChatLogLater(txtpaneLobbyChatLog,
								  getUIText("SysMsg_YourNickname") + convTripCode(NetUtil.urlDecode(message[1])), Color.blue);
			addSystemChatLogLater(txtpaneLobbyChatLog, getUIText("SysMsg_YourUID") + netPlayerClient.getPlayerUID(), Color.blue);

			addSystemChatLogLater(txtpaneLobbyChatLog, getUIText("SysMsg_SendRuleDataStart"), Color.blue);
			sendMyRuleDataToServer();
		}
		// Login failure
		if(message[0].equals("loginfail")) {
			setLobbyButtonsEnabled(0);

			if((message.length > 1) && message[1].equals("DIFFERENT_VERSION")) {
				String strClientVer = String.valueOf(GameManager.getVersionMajor());
				String strServerVer = message[2];
				String strErrorMsg = String.format(getUIText("SysMsg_LoginFailDifferentVersion"), strClientVer, strServerVer);
				addSystemChatLogLater(txtpaneLobbyChatLog, strErrorMsg, Color.red);
			} else if((message.length > 1) && message[1].equals("DIFFERENT_BUILD")) {
				String strClientBuildType = GameManager.getBuildTypeString();
				String strServerBuildType = message[2];
				String strErrorMsg = String.format(getUIText("SysMsg_LoginFailDifferentBuild"), strClientBuildType, strServerBuildType);
				addSystemChatLogLater(txtpaneLobbyChatLog, strErrorMsg, Color.red);
			} else {
				String reason = "";
				for(int i = 1; i < message.length; i++) {
					reason += message[i] + " ";
				}
				addSystemChatLogLater(txtpaneLobbyChatLog, getUIText("SysMsg_LoginFail") + reason, Color.red);
			}
		}
		// Banned
		if(message[0].equals("banned")) {
			setLobbyButtonsEnabled(0);

			Calendar cStart = GeneralUtil.importCalendarString(message[1]);
			Calendar cExpire = ((message.length > 2) && (message[2].length() > 0)) ? GeneralUtil.importCalendarString(message[2]) : null;

			String strStart = (cStart != null) ? GeneralUtil.getCalendarString(cStart) : "???";
			String strExpire = (cExpire != null) ? GeneralUtil.getCalendarString(cExpire) : getUIText("SysMsg_Banned_Permanent");

			addSystemChatLogLater(txtpaneLobbyChatLog, String.format(getUIText("SysMsg_Banned"), strStart, strExpire), Color.red);
		}
		// Rule dataTransmission success
		if(message[0].equals("ruledatasuccess")) {
			addSystemChatLogLater(txtpaneLobbyChatLog, getUIText("SysMsg_SendRuleDataOK"), Color.blue);

			// ListenerCall
			for(NetLobbyListener l: listeners) {
				l.netlobbyOnLoginOK(this, netPlayerClient);
			}
			if(netDummyMode != null) netDummyMode.netlobbyOnLoginOK(this, netPlayerClient);

			setLobbyButtonsEnabled(1);
		}
		// Rule dataTransmission failure
		if(message[0].equals("ruledatafail")) {
			sendMyRuleDataToServer();
		}
		// Rule receive (for rule-locked games)
		if(message[0].equals("rulelock")) {
			if(ruleOptLock == null) ruleOptLock = new RuleOptions();

			String strRuleData = NetUtil.decompressString(message[1]);

			CustomProperties prop = new CustomProperties();
			prop.decode(strRuleData);
			ruleOptLock.readProperty(prop, 0);

			log.info("Received rule data (" + ruleOptLock.strRuleName + ")");
		}
		// Rated-game rule list
		if(message[0].equals("rulelist")) {
			int style = Integer.parseInt(message[1]);

			if(style < listRatedRuleName.length) {
				listRatedRuleName[style].clear();

				for(int i = 0; i < message.length - 2; i++) {
					String name = NetUtil.urlDecode(message[2 + i]);
					listRatedRuleName[style].add(name);
				}
			}

			if(style == 0) {
				listmodelCreateRoom1PRuleList.clear();
				listmodelCreateRoom1PRuleList.addElement(getUIText("CreateRoom1P_YourRule"));
				listboxCreateRoom1PRuleList.setSelectedIndex(0);

				for(int i = 0; i < listRatedRuleName[style].size(); i++) {
					String name = (String)listRatedRuleName[style].get(i);
					listmodelCreateRoom1PRuleList.addElement(name);
				}

				listboxCreateRoom1PRuleList.setSelectedValue(propConfig.getProperty("createroom1p.listboxCreateRoom1PRuleList.value", ""), true);
			}
		}
		// PlayerList
		if(message[0].equals("playerlist") || message[0].equals("playerupdate") ||
		   message[0].equals("playernew") || message[0].equals("playerlogout"))
		{
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateLobbyUserList();
				}
			});

			if(tabLobbyAndRoom.isEnabledAt(1)) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						updateRoomUserList();
					}
				});

				if(message[0].equals("playerlogout")) {
					NetPlayerInfo p = new NetPlayerInfo(message[1]);
					NetPlayerInfo p2 = netPlayerClient.getYourPlayerInfo();
					if((p != null) && (p2 != null) && (p.roomID == p2.roomID)) {
						String strTemp = "";
						if(p.strHost.length() > 0) {
							strTemp = String.format(getUIText("SysMsg_LeaveRoomWithHost"), getPlayerNameWithTripCode(p), p.strHost);
						} else {
							strTemp = String.format(getUIText("SysMsg_LeaveRoom"), getPlayerNameWithTripCode(p));
						}
						addSystemChatLogLater(txtpaneRoomChatLog, strTemp, Color.blue);
					}
				}
			}
		}
		// PlayerEntering a room
		if(message[0].equals("playerenter")) {
			int uid = Integer.parseInt(message[1]);
			NetPlayerInfo pInfo = netPlayerClient.getPlayerInfoByUID(uid);

			if(pInfo != null) {
				String strTemp = "";
				if(pInfo.strHost.length() > 0) {
					strTemp = String.format(getUIText("SysMsg_EnterRoomWithHost"), getPlayerNameWithTripCode(pInfo), pInfo.strHost);
				} else {
					strTemp = String.format(getUIText("SysMsg_EnterRoom"), getPlayerNameWithTripCode(pInfo));
				}
				addSystemChatLogLater(txtpaneRoomChatLog, strTemp, Color.blue);
			}
		}
		// PlayerWithdrawal
		if(message[0].equals("playerleave")) {
			int uid = Integer.parseInt(message[1]);
			NetPlayerInfo pInfo = netPlayerClient.getPlayerInfoByUID(uid);

			if(pInfo != null) {
				String strTemp = "";
				if(pInfo.strHost.length() > 0) {
					strTemp = String.format(getUIText("SysMsg_LeaveRoomWithHost"), getPlayerNameWithTripCode(pInfo), pInfo.strHost);
				} else {
					strTemp = String.format(getUIText("SysMsg_LeaveRoom"), getPlayerNameWithTripCode(pInfo));
				}
				addSystemChatLogLater(txtpaneRoomChatLog, strTemp, Color.blue);
			}
		}
		// Change team
		if(message[0].equals("changeteam")) {
			int uid = Integer.parseInt(message[1]);
			NetPlayerInfo pInfo = netPlayerClient.getPlayerInfoByUID(uid);

			if(pInfo != null) {
				String strTeam = "";
				String strTemp = "";

				if(message.length > 3) {
					strTeam = NetUtil.urlDecode(message[3]);
					strTemp = String.format(getUIText("SysMsg_ChangeTeam"), getPlayerNameWithTripCode(pInfo), strTeam);
				} else {
					strTemp = String.format(getUIText("SysMsg_ChangeTeam_None"), getPlayerNameWithTripCode(pInfo));
				}

				addSystemChatLogLater(getCurrentChatLogTextPane(), strTemp, Color.blue);
			}
		}
		// Room list
		if(message[0].equals("roomlist")) {
			int size = Integer.parseInt(message[1]);

			tablemodelRoomList.setRowCount(0);
			for(int i = 0; i < size; i++) {
				NetRoomInfo r = new NetRoomInfo(message[2 + i]);
				tablemodelRoomList.addRow(createRoomListRowData(r));
			}
		}
		// Receive presets
		if(message[0].equals("ratedpresets")) {
			if (currentScreenCardNumber == SCREENCARD_CREATERATED_WAITING) {
				if (message.length == 1) {
					currentViewDetailRoomID = -1;
					setCreateRoomUIType(false, null);
					changeCurrentScreenCard(SCREENCARD_CREATEROOM);
				} else {
					comboboxCreateRatedPresets.removeAllItems();
					String preset;
					for (int i = 1; i < message.length; i++) {
						preset = NetUtil.decompressString(message[i]);
						NetRoomInfo r = new NetRoomInfo(preset);
						presets.add(r);
						comboboxCreateRatedPresets.addItem(r.strName);
					}
					changeCurrentScreenCard(SCREENCARD_CREATERATED);
				}
			}
		}
		// New room appearance
		if(message[0].equals("roomcreate")) {
			NetRoomInfo r = new NetRoomInfo(message[1]);
			tablemodelRoomList.addRow(createRoomListRowData(r));
		}
		// Room information update
		if(message[0].equals("roomupdate")) {
			NetRoomInfo r = new NetRoomInfo(message[1]);
			int columnID = tablemodelRoomList.findColumn(getUIText(ROOMTABLE_COLUMNNAMES[0]));

			for(int i = 0; i < tablemodelRoomList.getRowCount(); i++) {
				String strID = (String)tablemodelRoomList.getValueAt(i, columnID);
				int roomID = Integer.parseInt(strID);

				if(roomID == r.roomID) {
					String[] rowData = createRoomListRowData(r);
					for(int j = 0; j < rowData.length; j++) {
						tablemodelRoomList.setValueAt(rowData[j], i, j);
					}
					break;
				}
			}
		}
		// Annihilation Room
		if(message[0].equals("roomdelete")) {
			NetRoomInfo r = new NetRoomInfo(message[1]);
			int columnID = tablemodelRoomList.findColumn(getUIText(ROOMTABLE_COLUMNNAMES[0]));

			for(int i = 0; i < tablemodelRoomList.getRowCount(); i++) {
				String strID = (String)tablemodelRoomList.getValueAt(i, columnID);
				int roomID = Integer.parseInt(strID);

				if(roomID == r.roomID) {
					tablemodelRoomList.removeRow(i);
					break;
				}
			}

			if((r.roomID == currentViewDetailRoomID) && (currentScreenCardNumber == SCREENCARD_CREATEROOM)) {
				changeCurrentScreenCard(SCREENCARD_LOBBY);
			}
		}
		// Successfully create and enter Room
		if(message[0].equals("roomcreatesuccess") || message[0].equals("roomjoinsuccess")) {
			int roomID = Integer.parseInt(message[1]);
			int seatID = Integer.parseInt(message[2]);
			int queueID = Integer.parseInt(message[3]);

			netPlayerClient.getYourPlayerInfo().roomID = roomID;
			netPlayerClient.getYourPlayerInfo().seatID = seatID;
			netPlayerClient.getYourPlayerInfo().queueID = queueID;

			if(roomID != -1) {
				NetRoomInfo roomInfo = netPlayerClient.getRoomInfo(roomID);
				NetPlayerInfo pInfo = netPlayerClient.getYourPlayerInfo();

				if((seatID == -1) && (queueID == -1)) {
					String strTemp = String.format(getUIText("SysMsg_StatusChange_Spectator"), getPlayerNameWithTripCode(pInfo));
					addSystemChatLogLater(txtpaneRoomChatLog, strTemp, Color.blue);
					setRoomJoinButtonVisible(true);
				} else if(seatID == -1) {
					String strTemp = String.format(getUIText("SysMsg_StatusChange_Queue"), getPlayerNameWithTripCode(pInfo));
					addSystemChatLogLater(txtpaneRoomChatLog, strTemp, Color.blue);
					setRoomJoinButtonVisible(false);
				} else {
					String strTemp = String.format(getUIText("SysMsg_StatusChange_Joined"), getPlayerNameWithTripCode(pInfo));
					addSystemChatLogLater(txtpaneRoomChatLog, strTemp, Color.blue);
					setRoomJoinButtonVisible(false);
				}

				if((netPlayerClient != null) && (netPlayerClient.getRoomInfo(roomID) != null)) {
					if(netPlayerClient.getRoomInfo(roomID).singleplayer) {
						btnRoomButtonsJoin.setVisible(false);
						btnRoomButtonsSitOut.setVisible(false);
						btnRoomButtonsRanking.setVisible(false);
						gameStatCardLayout.show(subpanelGameStat, "GameStat1P");
					} else {
						btnRoomButtonsRanking.setVisible(netPlayerClient.getRoomInfo(roomID).rated);
						gameStatCardLayout.show(subpanelGameStat, "GameStatMP");
					}
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setRoomButtonsEnabled(true);
						updateRoomUserList();
					}
				});

				String strTitle = roomInfo.strName;
				this.setTitle(getUIText("Title_NetLobby") + " - " + strTitle);
				tabLobbyAndRoom.setTitleAt(1, getUIText("Lobby_Tab_Room") + strTitle);

				addSystemChatLogLater(txtpaneRoomChatLog, getUIText("SysMsg_RoomJoin_Title") + strTitle, Color.blue);
				addSystemChatLogLater(txtpaneRoomChatLog, getUIText("SysMsg_RoomJoin_ID") + roomInfo.roomID, Color.blue);
				if(roomInfo.ruleLock) {
					addSystemChatLogLater(txtpaneRoomChatLog, getUIText("SysMsg_RoomJoin_Rule") + roomInfo.ruleName, Color.blue);
				}

				setLobbyButtonsEnabled(2);
				changeCurrentScreenCard(SCREENCARD_LOBBY);

				// ListenerCall
				for(NetLobbyListener l: listeners) {
					l.netlobbyOnRoomJoin(this, netPlayerClient, roomInfo);
				}
				if(netDummyMode != null) netDummyMode.netlobbyOnRoomJoin(this, netPlayerClient, roomInfo);
			} else {
				addSystemChatLogLater(txtpaneRoomChatLog, getUIText("SysMsg_RoomJoin_Lobby"), Color.blue);

				this.setTitle(getUIText("Title_NetLobby"));
				tabLobbyAndRoom.setSelectedIndex(0);
				tabLobbyAndRoom.setEnabledAt(1, false);
				tabLobbyAndRoom.setTitleAt(1, getUIText("Lobby_Tab_NoRoom"));

				setLobbyButtonsEnabled(1);
				changeCurrentScreenCard(SCREENCARD_LOBBY);

				// ListenerCall
				for(NetLobbyListener l: listeners) {
					l.netlobbyOnRoomLeave(this, netPlayerClient);
				}
				if(netDummyMode != null) netDummyMode.netlobbyOnRoomLeave(this, netPlayerClient);
			}
		}
		// Entry Room failure
		if(message[0].equals("roomjoinfail")) {
			addSystemChatLogLater(txtpaneRoomChatLog, getUIText("SysMsg_RoomJoinFail"), Color.red);
		}
		// Kicked from a room
		if(message[0].equals("roomkicked")) {
			String strKickMsg = String.format(getUIText("SysMsg_Kicked_" + message[1]), NetUtil.urlDecode(message[3]), message[2]);
			addSystemChatLogLater(txtpaneLobbyChatLog, strKickMsg, Color.red);
		}
		// Map receive
		if(message[0].equals("map")) {
			String strDecompressed = NetUtil.decompressString(message[1]);
			String[] strMaps = strDecompressed.split("\t");

			mapList.clear();

			int maxMap = strMaps.length;
			for(int i = 0; i < maxMap; i++) {
				mapList.add(strMaps[i]);
			}

			log.debug("Received " + mapList.size() + " maps");
		}
		// Lobby chat
		if(message[0].equals("lobbychat")) {
			int uid = Integer.parseInt(message[1]);
			NetPlayerInfo pInfo = netPlayerClient.getPlayerInfoByUID(uid);

			if(pInfo != null) {
				Calendar calendar = GeneralUtil.importCalendarString(message[3]);
				String strMsgBody = NetUtil.urlDecode(message[4]);
				addUserChatLogLater(txtpaneLobbyChatLog, getPlayerNameWithTripCode(pInfo), calendar, strMsgBody);
			}
		}
		// Room chat
		if(message[0].equals("chat")) {
			int uid = Integer.parseInt(message[1]);
			NetPlayerInfo pInfo = netPlayerClient.getPlayerInfoByUID(uid);

			if(pInfo != null) {
				Calendar calendar = GeneralUtil.importCalendarString(message[3]);
				String strMsgBody = NetUtil.urlDecode(message[4]);
				addUserChatLogLater(txtpaneRoomChatLog, getPlayerNameWithTripCode(pInfo), calendar, strMsgBody);
			}
		}
		// Lobby chat/Room chat (history)
		if(message[0].equals("lobbychath") || message[0].equals("chath")) {
			String strUsername = convTripCode(NetUtil.urlDecode(message[1]));
			Calendar calendar = GeneralUtil.importCalendarString(message[2]);
			String strMsgBody = NetUtil.urlDecode(message[3]);
			JTextPane txtpane = message[0].equals("lobbychath") ? txtpaneLobbyChatLog : txtpaneRoomChatLog;
			addRecordedUserChatLogLater(txtpane, strUsername, calendar, strMsgBody);
		}
		// Participation status change
		if(message[0].equals("changestatus")) {
			int uid = Integer.parseInt(message[2]);
			NetPlayerInfo pInfo = netPlayerClient.getPlayerInfoByUID(uid);

			if(pInfo != null) {
				if(message[1].equals("watchonly")) {
					String strTemp = String.format(getUIText("SysMsg_StatusChange_Spectator"), getPlayerNameWithTripCode(pInfo));
					addSystemChatLogLater(txtpaneRoomChatLog, strTemp, Color.blue);
					if(uid == netPlayerClient.getPlayerUID()) setRoomJoinButtonVisible(true);
				} else if(message[1].equals("joinqueue")) {
					String strTemp = String.format(getUIText("SysMsg_StatusChange_Queue"), getPlayerNameWithTripCode(pInfo));
					addSystemChatLogLater(txtpaneRoomChatLog, strTemp, Color.blue);
					if(uid == netPlayerClient.getPlayerUID()) setRoomJoinButtonVisible(false);
				} else if(message[1].equals("joinseat")) {
					String strTemp = String.format(getUIText("SysMsg_StatusChange_Joined"), getPlayerNameWithTripCode(pInfo));
					addSystemChatLogLater(txtpaneRoomChatLog, strTemp, Color.blue);
					if(uid == netPlayerClient.getPlayerUID()) setRoomJoinButtonVisible(false);
				}
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateRoomUserList();
				}
			});
		}
		// Automatically start timerStart
		if(message[0].equals("autostartbegin")) {
			String strTemp = String.format(getUIText("SysMsg_AutoStartBegin"), message[1]);
			addSystemChatLogLater(txtpaneRoomChatLog, strTemp, new Color(64, 128, 0));
		}
		// game start
		if(message[0].equals("start")) {
			addSystemChatLogLater(txtpaneRoomChatLog, getUIText("SysMsg_GameStart"), new Color(0, 128, 0));
			tablemodelGameStat.setRowCount(0);
			tablemodelGameStat1P.setRowCount(0);

			if(netPlayerClient.getYourPlayerInfo().seatID != -1) {
				btnRoomButtonsSitOut.setEnabled(false);
				btnRoomButtonsTeamChange.setEnabled(false);
				itemLobbyMenuTeamChange.setEnabled(false);
				roomTopBarCardLayout.first(subpanelRoomTopBar);
			}
		}
		// Death
		if(message[0].equals("dead")) {
			int uid = Integer.parseInt(message[1]);
			String name = convTripCode(NetUtil.urlDecode(message[2]));

			if(message.length > 6) {
				String strTemp = String.format(getUIText("SysMsg_KO"), convTripCode(NetUtil.urlDecode(message[6])), name);
				addSystemChatLogLater(txtpaneRoomChatLog, strTemp, new Color(0, 128, 0));
			}

			if(uid == netPlayerClient.getPlayerUID()) {
				btnRoomButtonsSitOut.setEnabled(true);
				btnRoomButtonsTeamChange.setEnabled(true);
				itemLobbyMenuTeamChange.setEnabled(true);
			}
		}
		// Game stats (Multiplayer)
		if(message[0].equals("gstat")) {
			String[] rowdata = new String[13];
			int myRank = Integer.parseInt(message[4]);

			rowdata[0] = Integer.toString(myRank);						// Rank
			rowdata[1] = convTripCode(NetUtil.urlDecode(message[3]));	// Name
			rowdata[2] = message[5];									// Attack count
			rowdata[3] = message[6];									// APL
			rowdata[4] = message[7];									// APM
			rowdata[5] = message[8];									// Line count
			rowdata[6] = message[9];									// LPM
			rowdata[7] = message[10];									// Piece count
			rowdata[8] = message[11];									// PPS
			rowdata[9] = GeneralUtil.getTime(Integer.parseInt(message[12]));	//  Time
			rowdata[10] = message[13];									// KO
			rowdata[11] = message[14];									// Win
			rowdata[12] = message[15];									// Games

			int insertPos = 0;
			for(int i = 0; i < tablemodelGameStat.getRowCount(); i++) {
				String strRank = (String)tablemodelGameStat.getValueAt(i, 0);
				int rank = Integer.parseInt(strRank);

				if(myRank > rank) {
					insertPos = i + 1;
				}
			}

			tablemodelGameStat.insertRow(insertPos, rowdata);

			if(writerRoomLog != null) {
				writerRoomLog.print("[" + getCurrentTimeAsString() + "] ");

				for(int i = 0; i < rowdata.length; i++) {
					writerRoomLog.print(rowdata[i]);
					if(i < rowdata.length - 1) writerRoomLog.print(",");
					else writerRoomLog.print("\n");
				}

				writerRoomLog.flush();
			}
		}
		// Game stats (Single player)
		if(message[0].equals("gstat1p")) {
			String strRowData = NetUtil.urlDecode(message[1]);
			String[] rowData = strRowData.split("\t");

			if(writerRoomLog != null) {
				writerRoomLog.print("[" + getCurrentTimeAsString() + "]\n");
			}

			tablemodelGameStat1P.setRowCount(0);
			for(int i = 0; i < rowData.length; i++) {
				String[] strTempArray = rowData[i].split(";");
				tablemodelGameStat1P.addRow(strTempArray);

				if((writerRoomLog != null) && (strTempArray.length > 1)) {
					writerRoomLog.print(" " + strTempArray[0] + ":" + strTempArray[1] + "\n");
				}
			}

			if(writerRoomLog != null) {
				writerRoomLog.flush();
			}
		}
		// game finished
		if(message[0].equals("finish")) {
			addSystemChatLogLater(txtpaneRoomChatLog, getUIText("SysMsg_GameEnd"), new Color(0, 128, 0));

			if((message.length > 3) && (message[3].length() > 0)) {
				boolean flagTeamWin = false;
				if(message.length > 4) flagTeamWin = Boolean.parseBoolean(message[4]);

				String strWinner = "";
				if(flagTeamWin) strWinner = String.format(getUIText("SysMsg_WinnerTeam"), NetUtil.urlDecode(message[3]));
				else strWinner = String.format(getUIText("SysMsg_Winner"), convTripCode(NetUtil.urlDecode(message[3])));
				addSystemChatLogLater(txtpaneRoomChatLog, strWinner, new Color(0, 128, 0));
			}

			btnRoomButtonsSitOut.setEnabled(true);
			btnRoomButtonsTeamChange.setEnabled(true);
			itemLobbyMenuTeamChange.setEnabled(true);
		}
		// Rating change
		if(message[0].equals("rating")) {
			String strPlayerName = convTripCode(NetUtil.urlDecode(message[3]));
			int ratingNow = Integer.parseInt(message[4]);
			int ratingChange = Integer.parseInt(message[5]);
			String strTemp = String.format(getUIText("SysMsg_Rating"), strPlayerName, ratingNow, ratingChange);
			addSystemChatLogLater(txtpaneRoomChatLog, strTemp, new Color(0, 128, 0));
		}
		// Multiplayer Leaderboard
		if(message[0].equals("mpranking")) {
			int style = Integer.parseInt(message[1]);
			int myRank = Integer.parseInt(message[2]);

			tablemodelMPRanking[style].setRowCount(0);

			String strPData = NetUtil.decompressString(message[3]);
			String[] strPDataA = strPData.split("\t");

			for(int i = 0; i < strPDataA.length; i++) {
				String[] strRankData = strPDataA[i].split(";");
				String[] strRowData = new String[MPRANKING_COLUMNNAMES.length];
				int rank = Integer.parseInt(strRankData[0]);
				if(rank == -1) {
					strRowData[0] = "N/A";
				} else {
					strRowData[0] = Integer.toString(rank + 1);
				}
				strRowData[1] = convTripCode(NetUtil.urlDecode(strRankData[1]));
				strRowData[2] = strRankData[2];
				strRowData[3] = strRankData[3];
				strRowData[4] = strRankData[4];
				tablemodelMPRanking[style].addRow(strRowData);
			}

			if(myRank == -1) {
				int tableRowMax = tablemodelMPRanking[style].getRowCount();
				tableMPRanking[style].getSelectionModel().setSelectionInterval(tableRowMax - 1, tableRowMax - 1);
			} else {
				tableMPRanking[style].getSelectionModel().setSelectionInterval(myRank, myRank);
			}
		}
		// Announcement from the admin
		if(message[0].equals("announce")) {
			String strTime = getCurrentTimeAsString();
			String strMessage = "[" + strTime + "]<ADMIN>:" + NetUtil.urlDecode(message[1]);
			addSystemChatLogLater(getCurrentChatLogTextPane(), strMessage, new Color(255,32,0));
		}
		// Single player replay download
		if(message[0].equals("spdownload")) {
			long sChecksum = Long.parseLong(message[1]);
			Adler32 checksumObj = new Adler32();
			checksumObj.update(NetUtil.stringToBytes(message[2]));

			if(checksumObj.getValue() == sChecksum) {
				String strReplay = NetUtil.decompressString(message[2]);
				CustomProperties prop = new CustomProperties();
				prop.decode(strReplay);

				try {
					FileOutputStream out = new FileOutputStream("replay/netreplay.rep");
					prop.store(out, "NullpoMino NetReplay from " + netPlayerClient.getHost());
					addSystemChatLog(getCurrentChatLogTextPane(), getUIText("SysMsg_ReplaySaved"), Color.magenta);
				} catch (IOException e) {
					log.error("Failed to write replay to replay/netreplay.rep", e);
				}
			}
		}

		// ListenerCall
		if(listeners != null) {
			for(NetLobbyListener l: listeners) {
				l.netlobbyOnMessage(this, netPlayerClient, message);
			}
		}
		if(netDummyMode != null) netDummyMode.netlobbyOnMessage(this, netPlayerClient, message);
	}

	/*
	 * When it is cut
	 */
	public void netOnDisconnect(NetBaseClient client, Throwable ex) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setLobbyButtonsEnabled(0);
				setRoomButtonsEnabled(false);
				tablemodelRoomList.setRowCount(0);
			}
		});

		if(ex != null) {
			addSystemChatLogLater(getCurrentChatLogTextPane(), getUIText("SysMsg_DisconnectedError") + "\n" + ex.getLocalizedMessage(), Color.red);
			log.info("Server Disconnected", ex);
		} else {
			addSystemChatLogLater(getCurrentChatLogTextPane(), getUIText("SysMsg_DisconnectedOK"), new Color(128, 0, 0));
			log.info("Server Disconnected (null)");
		}

		// ListenerCall
		if(listeners != null) {
			for(NetLobbyListener l: listeners) {
				if(l != null) {
					l.netlobbyOnDisconnect(this, netPlayerClient, ex);
				}
			}
		}
		if(netDummyMode != null) netDummyMode.netlobbyOnDisconnect(this, netPlayerClient, ex);
	}

	/**
	 * Add an new NetLobbyListener, but don't add NetDummyMode!
	 * @param l A NetLobbyListener to add
	 */
	public void addListener(NetLobbyListener l) {
		listeners.add(l);
	}

	/**
	 * Remove a NetLobbyListener from the listeners list
	 * @param l NetLobbyListener to remove
	 * @return true if removed, false if not found or already removed
	 */
	public boolean removeListener(NetLobbyListener l) {
		return listeners.remove(l);
	}

	/**
	 * Set new game mode
	 * @param m Mode
	 */
	public void setNetDummyMode(NetDummyMode m) {
		netDummyMode = m;
	}

	/**
	 * Get current game mode
	 * @return Current game mode
	 */
	public NetDummyMode getNetDummyMode() {
		return netDummyMode;
	}

	/**
	 * Main functioncount
	 * @param args CommandLinesArgumentcount
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure("config/etc/log.cfg");
		NetLobbyFrame frame = new NetLobbyFrame();
		frame.init();
		frame.setVisible(true);
	}

	/**
	 * Text input Pop-up for the fieldMenu
	 * <a href="http://terai.xrea.jp/Swing/DefaultEditorKit.html">Exhibit</a>
	 */
	protected class TextComponentPopupMenu extends JPopupMenu {
		private static final long serialVersionUID = 1L;

		private Action cutAction;
		private Action copyAction;
		@SuppressWarnings("unused")
		private Action pasteAction;
		private Action deleteAction;
		private Action selectAllAction;

		public TextComponentPopupMenu(final JTextComponent field) {
			super();

			add(cutAction = new AbstractAction(getUIText("Popup_Cut")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt) {
					field.cut();
				}
			});
			add(copyAction = new AbstractAction(getUIText("Popup_Copy")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt) {
					field.copy();
				}
			});
			add(pasteAction = new AbstractAction(getUIText("Popup_Paste")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt) {
					field.paste();
				}
			});
			add(deleteAction = new AbstractAction(getUIText("Popup_Delete")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt) {
					field.replaceSelection(null);
				}
			});
			add(selectAllAction = new AbstractAction(getUIText("Popup_SelectAll")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt) {
					field.selectAll();
				}
			});
		}

		@Override
		public void show(Component c, int x, int y) {
			JTextComponent field = (JTextComponent) c;
			boolean flg = field.getSelectedText() != null;
			cutAction.setEnabled(flg);
			copyAction.setEnabled(flg);
			deleteAction.setEnabled(flg);
			selectAllAction.setEnabled(field.isFocusOwner());
			super.show(c, x, y);
		}
	}

	/**
	 * Pop-up box for the listMenu
	 */
	protected class ListBoxPopupMenu extends JPopupMenu {
		private static final long serialVersionUID = 1L;

		private JList listbox;
		private Action copyAction;

		public ListBoxPopupMenu(JList l) {
			super();

			this.listbox = l;

			add(copyAction = new AbstractAction(getUIText("Popup_Copy")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent e) {
					if(listbox == null) return;
					Object selectedObj = listbox.getSelectedValue();

					if((selectedObj != null) && (selectedObj instanceof String)) {
						String selectedString = (String)selectedObj;
						StringSelection ss = new StringSelection(selectedString);
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(ss, ss);
					}
				}
			});
		}

		@Override
		public void show(Component c, int x, int y) {
			if(listbox.getSelectedIndex() != -1) {
				copyAction.setEnabled(true);
				super.show(c, x, y);
			}
		}
	}

	/**
	 * Pop-up list box for server selectionMenu
	 */
	protected class ServerSelectListBoxPopupMenu extends JPopupMenu {
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unused")
		private Action connectAction;
		@SuppressWarnings("unused")
		private Action deleteAction;
		@SuppressWarnings("unused")
		private Action setObserverAction;

		public ServerSelectListBoxPopupMenu() {
			super();

			add(connectAction = new AbstractAction(getUIText("Popup_ServerSelect_Connect")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent e) {
					serverSelectConnectButtonClicked();
				}
			});
			add(deleteAction = new AbstractAction(getUIText("Popup_ServerSelect_Delete")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent e) {
					serverSelectDeleteButtonClicked();
				}
			});
			add(setObserverAction = new AbstractAction(getUIText("Popup_ServerSelect_SetObserver")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent e) {
					serverSelectSetObserverButtonClicked();
				}
			});
		}

		@Override
		public void show(Component c, int x, int y) {
			if(listboxServerList.getSelectedIndex() != -1) {
				super.show(c, x, y);
			}
		}
	}

	/**
	 * For server selection list boxMouseAdapter
	 */
	protected class ServerSelectListBoxMouseAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if((e.getClickCount() == 2) && (e.getButton() == MouseEvent.BUTTON1)) {
				serverSelectConnectButtonClicked();
			}
		}
	}

	/**
	 * Room list tablePop-up forMenu
	 */
	protected class RoomTablePopupMenu extends JPopupMenu {
		private static final long serialVersionUID = 1L;

		private Action joinAction;
		private Action watchAction;
		private Action detailAction;

		public RoomTablePopupMenu() {
			super();

			add(joinAction = new AbstractAction(getUIText("Popup_RoomTable_Join")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt) {
					int row = tableRoomList.getSelectedRow();
					if(row != -1) {
						int columnID = tablemodelRoomList.findColumn(getUIText(ROOMTABLE_COLUMNNAMES[0]));
						String strRoomID = (String)tablemodelRoomList.getValueAt(row, columnID);
						int roomID = Integer.parseInt(strRoomID);
						joinRoom(roomID, false);
					}
				}
			});
			add(watchAction = new AbstractAction(getUIText("Popup_RoomTable_Watch")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt) {
					int row = tableRoomList.getSelectedRow();
					if(row != -1) {
						int columnID = tablemodelRoomList.findColumn(getUIText(ROOMTABLE_COLUMNNAMES[0]));
						String strRoomID = (String)tablemodelRoomList.getValueAt(row, columnID);
						int roomID = Integer.parseInt(strRoomID);
						joinRoom(roomID, true);
					}
				}
			});
			add(detailAction = new AbstractAction(getUIText("Popup_RoomTable_Detail")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt) {
					int row = tableRoomList.getSelectedRow();
					if(row != -1) {
						int columnID = tablemodelRoomList.findColumn(getUIText(ROOMTABLE_COLUMNNAMES[0]));
						String strRoomID = (String)tablemodelRoomList.getValueAt(row, columnID);
						int roomID = Integer.parseInt(strRoomID);
						viewRoomDetail(roomID);
					}
				}
			});
		}

		@Override
		public void show(Component c, int x, int y) {
			if(tableRoomList.getSelectedRow() != -1) {
				joinAction.setEnabled(true);
				watchAction.setEnabled(true);
				detailAction.setEnabled(true);
				super.show(c, x, y);
			}
		}
	}

	/**
	 * Room list tableUseMouseAdapter
	 */
	protected class RoomTableMouseAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if((e.getClickCount() == 2) && (e.getButton() == MouseEvent.BUTTON1)) {
				Point pt = e.getPoint();
				int row = tableRoomList.rowAtPoint(pt);

				if(row != -1) {
					int columnID = tablemodelRoomList.findColumn(getUIText(ROOMTABLE_COLUMNNAMES[0]));
					String strRoomID = (String)tablemodelRoomList.getValueAt(row, columnID);
					int roomID = Integer.parseInt(strRoomID);
					joinRoom(roomID, false);
				}
			}
		}
	}

	/**
	 * Room list tableUseKeyAdapter
	 */
	protected class RoomTableKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				e.consume();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				int row = tableRoomList.getSelectedRow();
				if(row != -1) {
					int columnID = tablemodelRoomList.findColumn(getUIText(ROOMTABLE_COLUMNNAMES[0]));
					String strRoomID = (String)tablemodelRoomList.getValueAt(row, columnID);
					int roomID = Integer.parseInt(strRoomID);
					joinRoom(roomID, false);
				}
				e.consume();
			}
		}
	}

	/**
	 * Pop-up display field for logMenu
	 */
	protected class LogPopupMenu extends JPopupMenu {
		private static final long serialVersionUID = 1L;

		private Action copyAction;
		private Action selectAllAction;
		@SuppressWarnings("unused")
		private Action clearAction;

		public LogPopupMenu(final JTextComponent field) {
			super();

			add(copyAction = new AbstractAction(getUIText("Popup_Copy")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt) {
					field.copy();
				}
			});
			add(selectAllAction = new AbstractAction(getUIText("Popup_SelectAll")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt) {
					field.selectAll();
				}
			});
			add(clearAction = new AbstractAction(getUIText("Popup_Clear")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt) {
					field.setText(null);
				}
			});
		}

		@Override
		public void show(Component c, int x, int y) {
			JTextComponent field = (JTextComponent) c;
			boolean flg = field.getSelectedText() != null;
			copyAction.setEnabled(flg);
			selectAllAction.setEnabled(field.isFocusOwner());
			super.show(c, x, y);
		}
	}

	/**
	 * Display field for logKeyAdapter
	 */
	protected class LogKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if( (e.getKeyCode() != KeyEvent.VK_UP) && (e.getKeyCode() != KeyEvent.VK_DOWN) &&
			    (e.getKeyCode() != KeyEvent.VK_LEFT) && (e.getKeyCode() != KeyEvent.VK_RIGHT) &&
			    (e.getKeyCode() != KeyEvent.VK_HOME) && (e.getKeyCode() != KeyEvent.VK_END) &&
			    (e.getKeyCode() != KeyEvent.VK_PAGE_UP) && (e.getKeyCode() != KeyEvent.VK_PAGE_DOWN) &&
			    ((e.getKeyCode() != KeyEvent.VK_A) || (e.isControlDown() == false)) &&
			    ((e.getKeyCode() != KeyEvent.VK_C) || (e.isControlDown() == false)) &&
			    (!e.isAltDown()) )
			{
				e.consume();
			}
		}
		@Override
		public void keyTyped(KeyEvent e) {
			e.consume();
		}
	}

	/**
	 * Popup menu for any table
	 */
	protected class TablePopupMenu extends JPopupMenu {
		private static final long serialVersionUID = 1L;

		private Action copyAction;

		public TablePopupMenu(final JTable table) {
			super();

			add(copyAction = new AbstractAction(getUIText("Popup_Copy")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent e) {
					int row = table.getSelectedRow();

					if(row != -1) {
						String strCopy = "";

						for(int column = 0; column < table.getColumnCount(); column++) {
							Object selectedObject = table.getValueAt(row, column);
							if(selectedObject instanceof String) {
								if(column == 0) {
									strCopy += (String)selectedObject;
								} else {
									strCopy += "," + (String)selectedObject;
								}
							}
						}

						StringSelection ss = new StringSelection(strCopy);
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(ss, ss);
					}
				}
			});
		}

		@Override
		public void show(Component c, int x, int y) {
			JTable table = (JTable) c;
			boolean flg = table.getSelectedRow() != -1;
			copyAction.setEnabled(flg);
			super.show(c, x, y);
		}
	}

	/**
	 * Rule entry for rule change screen
	 */
	protected class RuleEntry {
		/** File name */
		public String filename;
		/** File path */
		public String filepath;
		/** Rule name */
		public String rulename;
		/** Game style */
		public int style;
	}

	/**
	 * Each label of Image Combobox<br>
	 * <a href="http://www.javadrive.jp/tutorial/jcombobox/index20.html">Source</a>
	 */
	protected class ComboLabel {
		private String text = "";
		private Icon icon = null;

		public ComboLabel() {
		}

		public ComboLabel(String text) {
			this.text = text;
		}

		public ComboLabel(Icon icon) {
			this.icon = icon;
		}

		public ComboLabel(String text, Icon icon) {
			this.text = text;
			this.icon = icon;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

		public void setIcon(Icon icon) {
			this.icon = icon;
		}

		public Icon getIcon() {
			return icon;
		}
	}

	/**
	 * ListCellRenderer for Image Combobox<br>
	 * <a href="http://www.javadrive.jp/tutorial/jcombobox/index20.html">Source</a>
	 */
	protected class ComboLabelCellRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 1L;

		public ComboLabelCellRenderer() {
			this.setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			ComboLabel data = (ComboLabel)value;
			setText(data.getText());
			setIcon(data.getIcon());

			if(isSelected) {
				setForeground(Color.white);
				setBackground(Color.black);
			} else {
				setForeground(Color.black);
				setBackground(Color.white);
			}

			return this;
		}
	}
}
