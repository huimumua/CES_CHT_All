Release version 1.00  (SVN revision 241)                 Date: 4 Oct 2011
--------------------                                     -----------------
-Initial version; based on HC API rel ver 2.02L
-Note: Please modify the configuration file app/zip_api_tst.cfg before running the test application.
       Set the Z/IP router configuration file ziprouter.cfg ZipUnsolicitedDestinationIp6 and
       ZipUnsolicitedDestinationPort = <ZipLanPort> accordingly.

Release version 1.01  (SVN revision 298)                 Date: 4 Nov 2011
--------------------                                     -----------------
-File menu items are based on initialization state.
-Created network operation notification callback thread in the test program to prevent
 execution of network function in the HC API notification callback thread that
 could cause program to crash.
-Added COMMAND_CLASS_BASIC to all the endpoints if node information frame has none.
-Added zwnet_version() for application to get the home controller API version and subversion.
-Get version of each endpoint command classes if the endpoint supports version command class.
-Added feature to truncate UTF-16 string in order to fit into the UTF-8 output buffer in plt_utf16_to_8().
-Added feature to check validity of UTF-8 string when set/get node name location.
-Added control of battery command class.
-Node info state-machine will query nodes for Z-wave library, protocol, and application versions.
-Defined HC_API_TYPE to indicate HC API type of the library.
-Return HC API type when the network initialization API zwnet_init() is called.
-Block node update of controller itself.
-Changed all the function names from zwif_xx_rpt_get to zwif_xx_get
-Application configuration file "zip_api_tst.cfg" entries have been reduced.  It now only
 requires Z/IP router IP address and host listening port settings.
-Acquire Z/IP PAN address by resolving controller node id into IPv6 site-local address.
-Check input values to the HC API interface commands.
-Changed some user interface texts to guide user on permissible input values.

Release version 1.02  (SVN revision 325)                 Date: 21 Dec 2011
--------------------                                     -----------------
-Fixed TO# 03386: After resetting Z/IP controller which was a secondary controller,
 it can't add node though it is now primary controller.
-Fixed bug: Fast calls between zwnet_init() and zwnet_close() will result in program crash.
-Fixed TO# 03387: Protocol version, application version, device class generic and specific
 were incorrect for Z/IP controller node.
-Fixed TO# 03389: After a add node timeout the Z/IP client sends an extra add node stop.
-Fixed TO# 03392: Change Z/IP retransmission timeout to 3.5s (Change FRAME_SEND_TIMEOUT_MIN to
 3500)
-TO# 03394: Added zwnet_send_nif() for sending node information frame.
-Removed socket option SO_REUSEADDR to disable application to listen at an already
 opened port.
-Added zwnet_get_user() to get user context of a network.
-Support migrate primary controller.
-Supported control of COMMAND_CLASS_THERMOSTAT_FAN_MODE.
-Supported control of COMMAND_CLASS_THERMOSTAT_FAN_STATE.
-Supported control of COMMAND_CLASS_THERMOSTAT_MODE.
-Supported control of COMMAND_CLASS_THERMOSTAT_OPERATING_STATE.
-Supported control of COMMAND_CLASS_THERMOSTAT_SETBACK.
-Supported control of COMMAND_CLASS_THERMOSTAT_SETPOINT.
-Supported control of COMMAND_CLASS_CLOCK.
-Supported control of COMMAND_CLASS_CLIMATE_CONTROL_SCHEDULE.
-Supported COMMAND_CLASS_MULTI_CMD.
-Supported control of COMMAND_CLASS_PROTECTION.
-Supported control of COMMAND_CLASS_APPLICATION_STATUS.
-Supported control of COMMAND_CLASS_INDICATOR.

Release version 1.03  (SVN revision 331)                 Date: 28 Dec 2011
--------------------                                     -----------------
-Shifted Z/IP address resolution functions from COMMAND_CLASS_ZIP to COMMAND_CLASS_ZIP_ND.
-Node info state-machine will check whether the command class in version report matches the
 requested command class to prevent looping.

Release version 1.04  (SVN revision 342)                 Date: 4 Jan 2012
--------------------                                     -----------------
-Fixed TO 03450 : When being included in unsecure mode by a secure controller, it failed to
 update the internal node list with the secure controller.
-Migrate of primary controller will handle the case of "migrated but insecure".

Release version 2.00  (SVN revision 351)                 Date: 12 Jan 2012
--------------------                                     -----------------
-Windows version added (same code base with Linux version)
-Promoted name/loc to node level, i.e. zwif_nameloc_set() is deprecated and
 zwnode_nameloc_set() will set the name and location in the node if the node
 has COMMAND_CLASS_NODE_NAMING interface.

Release version 2.01  (SVN revision 378)                 Date: 6 Feb 2012
--------------------                                     -----------------
-Fixed TO# 03448 : When Z/IP router is a secondary controller and can't get the node info
 from a device, the alive status of that node is wrongly stated as "alive".
-Device name and location on node level has been deprecated. Instead, it is moved to endpoint level.
 The function zwnode_nameloc_set() has been replaced by zwep_nameloc_set().
-Created endpoint node info state-machine to query endpoint's version, name and location info.
-Name and version stored in database to have priority over the values stored in the device.
-Adopted the suggestion from TO #03501 to display the controller network role under Help->Info.
-zwnet_migrate() will return error if the controller role is not "primary".
-zwnet_get_desc() will return also controller role.
-Get controller role from Z/IP router after "restore factory default".

Release version 2.02  (SVN revision 389)                 Date: 13 Feb 2012
--------------------                                     -----------------
-Modified Makefiles to support OPENWRT AR71XX platform.
-Added "INSTALL" file for instructions to compile the project.
-Deprecated HOST_BYTE_ORDER preprocessor definition to indicated target host processor endianess.
 The target host processor endianess is determined during run time.
-Fixed Home ID not displayed in correct endianess.

Release version 2.03  (SVN revision 415)                 Date: 23 Feb 2012
--------------------                                     -----------------
-Supported MAC OS X Lion.
-Modified report callback for supported thermostat fan operating modes to support OFF mode
 for version >= 2 (instead of 3 previously).

Release version 2.04  (SVN revision 427)                 Date: 2 Mar 2012
--------------------                                     -----------------
-Supported Sigma Tangox platform.
-Changed timeout for resetting Z/IP router to 9.5 seconds.
-Reduced the number of retries for sending frame to 1.
-Increased interval when retrying to send frame to 5 seconds.
-Reduced the maximum size of a multi-command buffer to 46 bytes
-After multi-command encapsulation is started, whenever the multi-command buffer is full,
 it will be sent regardless whether the "multi-command encapsulation stop" command is
 received.
-Enabled the use of "unused" state in zwif_thrmo_setb_set().
-Modified zwif_clmt_ctl_schd_set() to allow number of valid switchpoint equals to zero.
-Enabled the use of "unused" state in zwif_clmt_ctl_schd_ovr_set().
-Do not modify the override state when override type field is set to "No Override" in
 zwif_clmt_ctl_schd_ovr_set().

Release version 2.05  (SVN revision 457)                 Date: 9 Apr 2012
--------------------                                     -----------------
-Improved send data function by using condition variable to wait for condition to send data.
-Multi-command encapsulation will be turned on automatically if a device supports multi-cmd.
-If the multi-command encapsulation mode is on and command queuing (for wake up command class)
 is on, all the commands for sending will be queued until the wake up notification is received.
 All the commands in the queue will then be encapsulated using multi-cmd for sending.
-If the multi-command encapsulation mode is on and command queuing (for wake up command class)
 is off, all the commands for sending will be sent after 1 second timeout.  If there is only one
 command for sending, the command will not be encapsulated using multi-cmd; otherwise, they will
 be sent using multi-cmd encapsulation.
-Allow application to check the current state of the multi-cmd mode.
-Support waiting for response (report) to a GET command before sending another command
 in nodes that requires wakeup beam.
-Assign return route will only be sent after the group add command has completed.  This
 prevents the assign return route messages from interrupting the security message nonce get,
 nonce report, security encapsulation sequence.
-Increase the maximum wait time for sending a command from 3 seconds to 7 seconds, due to Z/IP
 router requires about 5 seconds to send assign return route for node which requires wakeup beam.
-Sending a command will wait until the completion of previous add/delete return route.
-Added a thread in application layer to callback higher layer to report send completion status.
 This allows the callback function to send multiple commands without blocking the subsequent
 send completion status.
-On Linux platform, socket descriptor will be set with FD_CLOSEXEC option which specifies
 that the file descriptor should be closed when an exec function is invoked.
-Use WSAStringToAddress() to replace inet_pton() for WindowsXP SP3 compatibility.

Release version 2.06  (SVN revision 479)                 Date: 19 Apr 2012
--------------------                                     -----------------
-Changed wait-for-response (report) timeout to 10 seconds for nodes that requires wakeup beam.
-Start wait-for-response (report) timer after the GET command send status is received.

Release version 2.07  (SVN revision 532)                 Date: 25 May 2012
--------------------                                     -----------------
-Changed Z/IP HCAPI to use Zip Inverse Node Solicitation Command for resolving all the nodes
 into IPv6 addresses.
-Replaced the memmem() (a GNU extension library function) with ported code.
-Changed the security scheme 0 marker in NODE_INFO_CACHED_REPORT to 0xF100 to be compatible
 with "Initial Z/IP Gateway" version 1.1
-Changed COMMAND_CLASS_NETWORK_MANAGEMENT_PRIMARY from 0x33 to 0x54
-Removed COMMAND_CLASS_NETWORK_MANAGEMENT_BRIDGE from the controller role.
-After receiving Z/IP frame which indicates destination host may have a long response time, Z/IP HCAPI will
 wait for a longer time for the response.
-Supported extended command class (2-byte command class).
-Added state-machine to query each endpoint for its supported security command classes
 after a node was added securely.
-Changed makefile to generate 32-bit binary code in OS X platform.

Release version 2.08  (SVN revision 552)                 Date: 26 Jun 2012
--------------------                                     -----------------
-Supported IPv4.
-Supported Z/IP IPv4/IPv6 gateway discovery.
-Changed "ZipRouterIp6" entry in configuration file to "ZipRouterIP" for setting Z/IP router IPv4
 or IPv6 address. If this field equals to 0.0.0.0 or :: , the Z/IP router IP will be acquired by
 using Z/IP IPv4/IPv6 gateway discovery respectively.

Release version 2.09  (SVN revision 562)                 Date: 5 Jul 2012
--------------------                                     -----------------
-Map thermostat setpoint supported type 0 to type 1 for Danfoss Living Connect workaround.

Release version 3.00  (SVN revision 631)                 Date: 14 Sep 2012
--------------------                                     -----------------
-Supported ZIPR with added axTLS library.
-Modified zwnet_get_desc() to return user specified information and platform context.
-Modified Makefiles to support linking and compiling with axTLS library when final
 target is ZIP Portal.
-Added variable fields in configuration file (zip_api_tst.cfg) to support ZIPR profile.
-Added remote text I/O client program in directory rmt_clnt.
-Changed all the text I/O functions to send and received text through UDP.  A remote
 text I/O client is required to connect with the Z/IP HC API instance.
-Support ZIPR firmware upgrade.
-Support ZIPR keep alive message.
-Support ZIPR firmware upgrade on IPv4 / IPv6 without going through portal.
-Fixed sequence number update for 'support' of simple AV Set commands.
-Fixed Simple AV Supported Get/Report.
-Fixed TO# 03767- Crash when Z/IP gw discovery is stopped immediately after the discovery
 is started.
-Support Z/IP gw discovery in the TCP tunnel.
-Created portal client profile management API to add, remove, find and list client profile.
-Modified zw_replace_failed_node() to include mode for start/stop. This allows application to
 cancel an uncompleted replace failed node id operation.

Release version 3.01  (SVN revision 645)                 Date: 3 Oct 2012
--------------------                                     -----------------
-Changed Makefile to generate test application binary zip_portal_tst when target is portal.
-Changed the TCP tunnel listening port number to 44123
-Changed session timeout to 24 hr, i.e. CONFIG_SSL_EXPIRY_TIME = 24
-Let the application to reject gateway connection in the portal callback.
-Added zwportal_clnt_conn_close() API for closing portal client TLS connection.

Release version 3.02  (SVN revision 680)                 Date: 22 Oct 2012
--------------------                                     -----------------
-Supported auto configuration of slave devices with wake up and sensor command classes
 when added to the Z-wave network.
-Added entries in configuration file "zip_api_tst.cfg" for configuring default wake up interval,
 and device configuration file path.
-For sensor configuration, default association group id is 1.
-Remove duplicate command in command queues.
-Added device category definitions (DEV_XXX).
-During initialization, if a node is sleeping or powered off, the "Z/IP Inverse Node Solicitation Command"
 sent to the Z/IP gateway would likely receive the response of INFORMATION_OBSOLETE for that node. HC
 API will ignore this message and proceed to get the IP of another node. The sleeping or powered off
 node will be marked as "down/sleeping".
-Enhanced the determination method of a node's alive status.  The new method uses the Z-wave ACK to the
 NOP command to decide whether the node is alive or "down/sleeping".

Release version 3.03  (SVN revision 717)                 Date: 11 Nov 2012
--------------------                                     -----------------
-Fixed TO#3868 - signal SIGPIPE causes the application to crash.
-Changed zwif_sensor_get() API to support multilevel sensor command class version 5.
-Supported multilevel sensor command class version 5
-Changed node info state-machine to query multilevel sensor supported types and units.
-Fixed TO# 03686:Persistent storage created by older versions are treated as corrupt.
-Changed persistent storage format.  All tags and length fields are stored in big-endian format
 Added file version field for future compatibility. Append CRC16 checksum at the end of file to
 improve integrity of the persistent data.
-Fixed TO# 03875 - "Errors -115 waiting for transmit complete callback function retry later"
 occur when including a static controller to UPnP Bridge having 10 virtual nodes.

Release version 3.04  (SVN revision 762)                 Date: 7 Dec 2012
--------------------                                     -----------------
-Fixed TO# 03886: Wake up interval is set to 0 when device specific configuration has no records.
-Fixed TO# 03900: Deadlock scenario during node inclusion.
-Fixed TO# 03901: Initialization fails to complete when persistent storage has duplicate set
 of interfaces in an endpoint.
-Fixed TO# 03907: Repeated sending of KeepAlive AV cmd does not stop with KeyUp cmd
 in certain timing conditon.
-Added bitmap to indicate fields validity in device specific configuration record.
-Changed device matching algorithm to support "don't care" cases in device specific
 configuration records. Valid "don't care" combinations in the format (Manf id, Product Type, Product id)
 are : (X , X, X), (V, X, X) and (V, V, X)  where V="valid value"; X="don't care".
-Node will be updated with device category from the device specific configuration record.
-Provided flexibility to user application for choosing between built-in device specific configurations management,
 or user supplied device specific configurations management functions.

Release version 3.05  (SVN revision 792)                 Date: 7 Jan 2013
--------------------                                     -----------------
-Fixed bug: The name and location from the persistent storage will be lost if node update is performed.
-Added basic device class entry in persistent storage.

Release version 3.06  (SVN revision 816)                 Date: 15 Feb 2013
--------------------                                     -----------------
-Supported COMMAND_CLASS_FIRMWARE_UPDATE_MD version 3.
-Supported COMMAND_CLASS_ZIP_GATEWAY.
-Supported COMMAND_CLASS_ZIP_PORTAL.
-Supported COMMAND_CLASS_ZWAVEPLUS_INFO
-Supported COMMAND_CLASS_DEVICE_RESET_LOCALLY
-Supported CRC16_ENCAP
-Supported COMMAND_CLASS_ASSOCIATION_GRP_INFO
-Supported COMMAND_CLASS_VERSION version 2.
-Auto-configure lifeline group.
-Auto-remove node upon receiving DEVICE_RESET_LOCALLY_NOTIFICATION
-Modified persistent storage to support field that is more than 255 bytes.
For portal version:
-Added PIN field in user database for authentication of Z/IP gateway.
-Expanded gateway id to 8-byte IEEE EUI-64 identifier format.
-Using SSL certificate serial number for authentication of Z/IP gateway.

Release version 3.07  (SVN revision 840)                 Date: 6 Mar 2013
--------------------                                     -----------------
-Removed obsolete zwnet_fw_ver_get, zwnet_fw_update and zwnet_fw_updt_sts_get APIs.
-Removed zwif_gw_cfg_reset API which was meant for testing of ZIPR.
-Improved method to remove failed node when receiving DEVICE_RESET_LOCALLY_NOTIFICATION.
-Reject firmware update request if firmware file is 0 in size.
-Stop to send firmware fragment after receiving FIRMWARE_UPDATE_MD_STATUS_REPORT.
-Send gateway unregister to ZIPR if it's not found in portal database.
-Supported Z/IP command class version 2
-Enable sending of Z/IP Gateway and Portal command classes packet in excess of Z-wave packet size.
-Updated ZW_classcmd.h header file.

Release version 3.08  (SVN revision 851)                 Date: 14 Mar 2013
--------------------                                     -----------------
-Removed obsolete zwif_gw_cfg_reset API which was meant to testing old ZIPR firmware.
-Fixed bug node info state-machine fails to get detailed info from second node onwards
 during initialization.
-Supported header extension and option in Z/IP packet.
-Fixed TO# 4046: Firmware CC is not sent with secure origin flag

Release version 3.09  (SVN revision 897)                 Date: 15 Apr 2013
--------------------                                     -----------------
-Increased Z/IP router reset response time out to 15 seconds to support ZIPR with 500
 series Z-wave chip. Reported in TO# 04062.
-When sending multi-channel encapsulation frame, set source endpoint to 0.
-Added polling facility.
-Remove command recording facility.
-Added Z/IP gateway command class's unsolicited destination set/get commands.
-Auto-configure Z/IP gateway unsolicited destination address if it is unconfigured.
For portal version:
-Added configuration entries in the config file to configure Z/IP gateway's default
 IPv6 gateway, PAN prefix, unsolicited message destination IP and port.

Release version 3.10  (SVN revision 908)                 Date: 17 Apr 2013
--------------------                                     -----------------
-Fixed issue of couldn't get local IP address for auto-configure "Z/IP gateway unsolicited
 destination address" in Mac OSX platform.
-Fixed TO#4090: SIGABRT when Peer Name Length is greater than 57 for
 GATEWAY_PEER_REPORT command.
-Treat the "no response to unsolicited destination address get" as non-critical error during
 initialization.

Release version 3.11  (SVN revision 930)                 Date: 25 Apr 2013
--------------------                                     -----------------
-Work-around for Astral 2-gang dimmer which requires source and destination endpoint id to be
 same when sending command to it.
-When receives Z/IP Node Advertisement Command with status "INFORMATION_OK" but with invalid
 IP address of 0.0.0.0, will retry up to 5 times every 2 seconds until getting a valid IP address.
-Get more information about the Z/IP gateway after resetting network.
-Streamlined all the state-machines.

Release version 3.12  (SVN revision 952)                 Date: 8 May 2013
--------------------                                     -----------------
-Query supported type/feature of some command classes during initialization process and cache
 them for later use.
-Set learn mode using option ZW_SET_LEARN_MODE_NWI for inclusion, and using option
 ZW_SET_LEARN_MODE_CLASSIC for exclusion.
-When performing the association set to a device that supports Multi Channel Association Command Class,
 use endpoint id = 1 for controller node. This will force the multi-channel device to send multi-channel
 encapsulated report to the controller.
-The interface passed as parameter to zwif_group_add() and zwif_group_del() can either be Association
 command class or Multi Channel Association Command Class.  The APIs will try to find the suitable
 interface based on the endpoints to add/delete.

Release version 3.13  (SVN revision 963)                 Date: 14 May 2013
--------------------                                     -----------------
-Changed the numbers of Z/IP GW discovery callbacks from one to as many as the valid source
 host IP addresses.
-Renamed zwif_poll_rm_mul to zwnet_poll_rm_mul; zwif_poll_rm to zwnet_poll_rm.
-Added menu item "Device->Gateway discovery" for testing of Z/IP GW discovery after HCAPI was
 initialized.

Release version 3.14  (SVN revision 969)                 Date: 16 May 2013
--------------------                                     -----------------
For portal version:
-Portal listening port number is configurable instead of hardcoded 44123.
-Added entry "PortalPort" in configuration file to configure portal listening port.

Release version 3.15  (SVN revision 987)                 Date: 22 May 2013
--------------------                                     -----------------
-Check received message type against interface security property.
-Removed unsolicited IPv6 destination address/port from GATEWAY_CONFIGURATION_SET command.
-Set multi-command and  CRC-16 command classes to Z/IP gateway node information using
 COMMAND_CLASS_ZIP_GATEWAY->COMMAND_APPLICATION_NODE_INFO_SET.
-When receiving user code report with invalid user code (i.e. non ASCII digits), the report
 will be rejected.

Release version 3.16  (SVN revision 998)                 Date: 27 May 2013
--------------------                                     -----------------
-Query maximum supported user codes during initialization process and cache
 it for later use.
-Close firmware file after receiving FIRMWARE_UPDATE_MD_STATUS_REPORT.

Release version 3.17  (SVN revision 1071)                Date: 5 Jul 2013
--------------------                                     -----------------
-Supported Manufacturer Specific Command Class version 2.
-Removed support of CRC-16.
-Removed auto-configuration of default wakeup interval on device with WAKE UP command class.
-Removed the DefaultWkUpInterval entry in configuration file "zip_api_tst.cfg".
-Supported COMMAND_CLASS_POWERLEVEL (to control).
-Supported Association, AGI and reset locally command classes.
-Modified firmware upgrade completion status to include wait time for target to reboot,
 which is specified in COMMAND_CLASS_FIRMWARE_UPDATE_MD version 3.
-Added zwif_thrmo_setp_get_poll() API.
-Added zwnoded_t structure with sleep_cap (to indicate node is capable to sleep), sensor
 (to indicate node is FLIRS) and wkup_intv (to indicate the cached version of wake up
 interval).
For portal version:
-After the GATEWAY_CONFIGURATION_SET command is successful, use the portal IPv6 prefix as
 source address in the TLS secure tunnel.


Release version 3.18  (SVN revision 1101)                Date: 11 Jul 2013
--------------------                                     -----------------
-Added a generic callback function when sending reset locally command so that the Set default
 will be performed after the ACK (for reset locally command) from ZIPR is received.
-Workaround for Windows Winsock sendto() return o.k. but doesn't send the packet into the
 IP network.
-Added persistent storage for group 1 node id for lifeline usage.
-Modified zwnet_gw_discvr_start() to callback even if the system has no valid IP to facilitate
 gateway discovery.
-Supported VERSION_COMMAND_CLASS_GET command for those Command classes that are supported in
 HCAPI but push to ZIPR. (This fix will require ZIPR to forward the version command to HCAPI
 for those command classes that are supported by HCAPI.)

Release version 3.19  (SVN revision 1137)                Date: 19 Jul 2013
--------------------                                     -----------------
-Added file "cmdclass_info.txt" to document supported and control of command classes.
-Added valid_grp_cnt to if_grp_info_dat_t structure to indicate valid group info count.
-Changed Power level Interface APIs parameter names and documentation.
-Added control of Z-wave+ info command class version 2.
-Added control of multilevel sensor command class version 6.
-Changed zwif_sensor_get() and zwif_sensor_get_poll() to accept parameter type=0 as an
 indication to use SENSOR_MULTILEVEL_GET command version 1.
-Added zwif_sensor_sup_cache_get() to get supported sensor types and units from the cache.

Release version 3.20  (SVN revision 1189)                 Date: 2 Aug 2013
--------------------                                     -----------------
-When receiving MULTI_CHANNEL_ASSOCIATION_REPORT, check for controller's node id and set
 its endpoint id to zero.
-Changed zwif_group_del() to check for controller in the endpoint list.  If controller is
 present in the list, change controller's endpoint id to 1.
-Hide COMMAND_CLASS_VERSION interface on the Z/IP gateway node.
-Added control of COMMAND_CLASS_ALARM (a.k.a. COMMAND_CLASS_NOTIFICATION) version 3.
-Fixed TO# 4512: Unable to complete inclusion of AEON DSC24 and DSC25 devices.

Release version 3.21  (SVN revision 1200)                 Date: 7 Aug 2013
--------------------                                     -----------------
-Fixed bug: if notification report of Alarm command class version 3 with sequence flag set is
            received, the report will be discarded.
-Fixed TO#4516 "No ACK response from HCAPI ZIP Client after Wake up for MAC OSX".
-Workaround for OSX sendto() return o.k. but doesn't send the packet into the
 IP network.

Release version 3.22  (SVN revision 1211)                 Date: 13 Aug 2013
--------------------                                     -----------------
-Added enumerating of security supported command classes in a multichannel endpoint.
For portal version:
-Fixed bug: IPv6 packet is not constructed properly when send Z/IP ACK response.

Release version 3.23  (SVN revision 1228)                 Date: 19 Aug 2013
--------------------                                     -----------------
-Fixed TO# 04535 by removing checking of ASCII digits in USER_CODE_REPORT.
-Set controller node id into group 1 of all endpoints that support association or mult-channel
 association command class after the node is included into the network.
-Changed default firmware fragment size for version 1 and 2 to 40 bytes.
-Changed minimum firmware fragment size for version 3 to 40 bytes.
-Changed ZWNET_NODE_INFO_GET_DELAY to 0.5 second.

Release version 3.24  (SVN revision 1286)                 Date: 11 Sep 2013
--------------------                                     -----------------
-Added directory "openssl" for option to compile TLS using openssl library.
-Modified INSTALL file to include option to compile TLS using openssl library.
-Added entry "CAPemCertFile" in zip_api_tst.cfg to set the CA PEM certificate file path.
 This certificate is used by openssl library.
-Query for wakeup capabilities, supported meter type and meter descriptor during inclusion
 and cache it for later use.
-Fixed TO# 04673: Does not Assign Return Routes for association Set and Wakeup Set doing inclusion.
-Fixed TO# 04674: Does not send Wakeup No More Information.
-Fixed TO# 04676: Did not do meter get_sup during node update

Release version 3.25  (SVN revision 1343)                 Date: 30 Sep 2013
--------------------                                     -----------------
-Changed the interpretation of Manufacturer specific's device specific get/report as follows:
 The report should report the default device ID data and type field as they belong together.
 When being asked with device ID=0. I.e if ID=1 (serial number) is 'default' then a GET(id=0)
 should trigger a Report(Id=1, serial number data).
-Fixed TO# 04820:Unable to proceed with post inclusion process for battery operated device in Z-Ware.

Release version 3.26  (SVN revision 1392)                 Date: 10 Oct 2013
--------------------                                     -----------------
-Supported Android platform.
-Added API zwnet_pan_prefix_get() to get PAN prefix.

Release version 3.27  (SVN revision 1456)                 Date: 30 Oct 2013
--------------------                                     -----------------
-Fixed 64-bit Linux compatibility issues.

Release version 3.28  (SVN revision 1476)                 Date: 5 Nov 2013
--------------------                                     -----------------
-Fixed OpenSSL version portal cannot support ZIP GW discovery issue.

Release version 3.29  (SVN revision 1569)                 Date: 5 Dec 2013
--------------------                                     -----------------
-Reduce node/network update time by skipping other command class info queries if
 certain command class queries do not receive report from the node.
-Added openssl 64-bit library.

Release version 3.30  (SVN revision 1632)                 Date: 19 Dec 2013
--------------------                                     -----------------
-Use separate thread to execute network operation notification callback to avoid deadlock condition.
-Use separate thread to execute node add/remove/update notification callback to avoid deadlock condition.
-Accept DEVICE_RESET_LOCALLY_NOTIFICATION from slave device which has no
 COMMAND_CLASS_DEVICE_RESET_LOCALLY command class listed in its node information frame.
-Changed TLS and ZIPR handshaking timeout from 5 minutes to 8 minutes.

Release version 3.31  (SVN revision 1650)                 Date: 24 Dec 2013
--------------------                                     -----------------
-Removed the "alive" member from zwnoded_t structure.  Node alive status can be retrieved
 using the new APIs zwnet_node_sts_get() or zwnet_all_node_sts_get().

Release version 3.32                                      Date: 3 Jan 2014
--------------------                                     -----------------
-Added lock before call to ssl_read / ssl_write to prevent buffer corruption at the axTLS library.

Release version 3.33                                      Date: 13 Jan 2014
--------------------                                     -----------------
-Clear all node alive status after resetting the controller to factory default.

Release version 3.34                                      Date: 11 Mar 2014
--------------------                                     -----------------
-Changed the wait time in zwpoll_shutdown() and zwnet_exit() to be slightly more than
 the send data condition wait timeout value to avoid illegal access to freed memory.
-Added APIs zwnet_get_node_by_id(), zwnet_get_ep_by_id() and zwnet_get_if_by_id() to support
 scene module.
-Supported device specific information and configuration database in JSON format. Please read
 the readme.txt file in the config folder for more information.
For portal:
-Changed the way to lock ssl_read (from plt_mtx_lck() to plt_mtx_trylck()) to prevent blocking
 if the lock is held by ssl_write which may block if network cable is unplugged.
-Changed transport layer write thread to asynchronous cancelable thread to prevent the write
 thread from unable to terminate if it is executing ssl_write() which may block if network cable
 is unplugged.

-Fixed stress test thread does not exit graciously when network error occurs.
-Check for HCAPI exit signal while during retry sending command to node that requires wakeup beam.
-Fixed TO#4677 : Association & Configuration CC setting should be encodable in device dbase for
                 different devices for automatic post inclusion settings
-Fixed TO#5195 : ZIPR disconnecting from Portal after Z-Ware network had established
-Fixed TO#5220 : ASSOCIATION_V2 and MULTI_CHANNEL_ASSOCIATION_V2 unable to remove node
                 when Group Identifier is 0
-Fixed TO#5235: Reset device locally does not remove secure Z-Wave Plus node from network.

Release version 3.35                                      Date: 26 Mar 2014
--------------------                                     -----------------
-Fixed a FLIRs node sends "device reset locally notification" to controller while the controller
 is sending commands, the reset node is not removed from the controller network node list.
-Modified zwnet_init_t structure and zwdev_cfg_load() to support identification of error location
 in device specific configuration file while parsing it.
-Handle COMMAND_CLASS_NOTIFICATION->NOTIFICATION_REPORT special case where "Notification Status" = 0xFE.
 In this case no further fields will be appended to the report, thus the report command is structured
 as Notification Report (V1 Alarm Type = 0, V1 Alarm Level = 0, Zensor Net Source Node ID = 0,
 Notification Status = 0xFE).

Release version 3.36                                      Date: 16 Apr 2014
--------------------                                     -----------------
-Update Z/IP controller alive status as "alive" whenever any other node status is updated
 as "alive".
-Fixed TO #5359 : Controller/ZIPR is marked red after performing Network Update.
-Map alarm command class version 1 proprietary alarm type to Z-wave defined alarm type
 using device specific information and configuration database.
-Added Z-wave alarm type/events in "config/device_rec_constants.txt" for mapping of proprietary
 alarm type to Z-wave defined alarm type.
-Added "config/zwave_device_rec.txt" device database file that is based on real devices.
-Modified zwif_sensor_sup_get() and zwif_sensor_unit_get() to return cached supported sensor types
 and units if available.
-Map binary sensor command class version 1 binary sensor report to Z-wave defined binary sensor type
 using device specific information and configuration database.
-Fixed bug that if an endpoint does not have version command class listed, endpoint state-machine
 will not query detailed information of that endpoint.

Release version 3.37                                      Date: 16 May 2014
--------------------                                     -----------------
-Re-designed polling sub-system to provide callback on completion and fair polling among
 different nodes.
-Supported callback to user application on node alive status change.
-Prevent primary / SIS controller from entering into learn mode when there are slave nodes
 in its network.
-Update OpenSSL libraries and header files (32-bit and 64-bit versions) to version 1.0.1.g
 to fix "Heartbleed" security issue.

Release version 3.38                                      Date: 31 Jul 2014
--------------------                                     -----------------
-Added support for user application to set default handler for unhandled commands.
-Added support for user application to set user implemented command classes (including version
 and security attribute) in controller's NIF.
-Added support for preliminary Z/IP gateway wifi configuration.
-Remove duplicate functional interfaces at endpoint 0 for multi-channel slave device.
-Fixed TO# 05492 - HCAPI not sending poll and proceed to invoke completion call back for zwpoll
 request (12 polls 1sec interval).

Release version 3.39                                      Date: 29 Sep 2014
--------------------                                     -----------------
-Changed poll callback function zwpoll_cmplt_fn with additional parameter usr_token.
-ASSOCIATION_GROUP_INFO_REPORT uses profile AGI_PROFILE_GENERAL:AGI_PROFILE_ GENERAL_LIFELINE as defined in spec.
-Get manufacturer specific info such as vendor id, product id, serial number, etc from Z/IP gateway.
-Fixed TO#5562 - Endpoints names are not saved after Network Update.
-Fixed TO#5563 - After network update all endpoints are not listed for ZEmu.
-Send WAKE_UP_NO_MORE_INFORMATION to sleeping node after post-inclusion configuration has completed.
-Created state-machine to handle unsolicited node list report from ZIPR when it is assigned SIS role or
 a node is added/removed by other inclusion controller.  HCAPI will set lifeline in all “always on” devices
 after being assigned SIS role.
-Fixed TO#5611 - ZIPR displays 0 for ProductID and ProductType in EngrgUI.
-When execute zwnet_reset(), if HCAPI fails to send DEVICE_RESET_LOCALLY_NOTIFICATION, it will still proceed to reset itself
 and run the reset state-machine.

Release version 4.00                                      Date: 18 Nov 2014
--------------------                                     -----------------
-Support DTLS as secure transport layer (linux version only).
-Support mdns discovery of Z/IP gateway version 2 (linux version only).
-Increase the maximum number of gateways to discover for a each network interface to 100.

Release version 4.01                                      Date: 10 Dec 2014
--------------------                                     -----------------
-Added support of Z/IP gateway version 2 for MAC OSX, Android, Openwrt, Sigma SMP87XX Vantage and
 Sigma SMP86XX Tangox platforms. Please read the file "INSTALL" for build instructions.
-Modified Makefiles to fix compile options "-g -O0" (for debug) and "-O2" (for release) not passed to
 compiler.
-Support data persistent - prefetch device readings on inclusion, update when receiving reports,
 save them on exit. Cached command classes are door lock, binary/multilevel switch, multilevel sensor,
 thermostat mode, operating state and setpoint.
-Added support to store network/user preferences into persistent storage.
-Added entry "PrefDir" in configuration file "zip_api_tst.cfg" for setting of directory for storing
 network/user preference files.

Release version 4.02                                      Date: 30 Dec 2014
--------------------                                     -----------------
-Added copyright header to all files.
-Added support of generation of documentation by Doxygen. Please read the doc\doxygen\Readme.txt.
-Return error code ZW_ERR_CACHE_AVAIL instead of ZW_ERR_QUEUED when an interface API has a cached data.
-Upgraded axTLS library to version 1.5.0
-Removed axTLS library from project directory, it will be installed as an external project.
-Added script file install_axtls_lib.sh for installing and patching axTLS library.
-Added script file build_axtls_lib.sh for building axTLS library.
-Modified instruction to build portal version (see the file INSTALL).

Release version 4.03                                      Date: 28 Jan 2015
--------------------                                     -----------------
-Changed zwif_group_del() and zwrep_group_fn parameter to allow entering and listing of non-existent
 node/endpoint as group member.
-Changed zwif_group_del() to allow deletion of all members in a group for Association Command Class version 1
 interface.
-Fixed TO#5830 - Unable to associate new node when previous associated slave is removed from network.
-Added support of BeagleBone Black platform.
-Removed parameter "hc_api_type" from zwnet_init().
-Updated ZW_classcmd.h with definition of COMMAND_CLASS_MAILBOX.
-Corrected supported version of COMMAND_CLASS_METER to version 3 in file "cmdclass_info.txt".
-Extended data persistent support to binary sensor and meter command classes.
-Added additional checking for lifeline:  After adding controller id to group 1 (lifeline),
 read back from the device for verification. If group 1 is full, erase group 1 and add controller id again.
-Removed test application "app" and "rmt_clnt" directories.
-Added sample applications in "demos" directory.
-Removed support of axTLS library for portal version.
-Removed portal SSL certificates and key and test application configuration file from "config" directory.

Release version 4.04                                      Date: 13 Feb 2015
--------------------                                     -----------------
-Removed "openssl" directory.
-Added script "install_openssl_lib.sh" to install openssl library source.
-Added script "build_openssl_lib.sh" to build openssl library from source.
-Modified instructions for building portal library using openssl in "INSTALL" file.
-Before retransmission of Z/IP version 2 packet, close the existing DTLS session and then establish a new DTLS
 session for the sending of Z/IP packet.
-Use "COMMAND_CLASS_ZIP->COMMAND_ZIP_KEEP_ALIVE" for sending keep alive message.
-Added support to response to "COMMAND_CLASS_ZIP->COMMAND_ZIP_KEEP_ALIVE" request.
-Changed dtls handshake message timeout to 16 seconds.

Release version 4.05                                      Date: 24 Feb 2015
--------------------                                     -----------------
-Added "@ingroup zwarecapi" in "@defgroup" for Doxygen documentation.
-Fixed bug when slave device does not respond to a command during inclusion, this causes timeout in endpoint
 info state-machine and it continues to run though it should be stopped.

Release version 4.06                                      Date: 6 Mar 2015
--------------------                                     -----------------
-Extended data persistent (caching) support to battery and basic command classes.
-Modified file "INSTALL" and script "build_external_lib.sh" to warn of unsupported (experimental) platforms.
-Modified cyassl library to enable dtls client to support both handshake with/without "Hello verify request".
-Added checking of dtls pre-shared key MUST be at least 16 bytes (according to SDS12938 - Z/IP LAN Security)
-Disable “PSK_identity” and the identity_hint in dtls handsake message (according to SDS12938 - Z/IP LAN Security).
-Move the process of multi-channel encapsulation to Z/IP gateway by setting the Z/IP packet header source/destination
 endpoint fields.

Release version 4.07                                      Date: 13 Mar 2015
--------------------                                     -----------------
-Support version 2 of door lock command class.
-Support version 4 of thermostat fan mode command class.
-Extended data persistent (caching) support to doorlock (configuration report), thermostat fan mode
 and thermostat fan state.
-Fixed bug: multi-command encapsulation destined for real endpoint was sent wrongly to root device.

Release version 4.08                                      Date: 29 May 2015
--------------------                                     -----------------
-Updated opnessl library to version 1.0.1m
-Added support for Notification/Alarm CC up to version 5.
-Rewrote Alarm CC event parameter parsing due to the change in newer CC spec.
-Implemented 2-level data caching mechanism to store unknown-size array with variable-size data.
-Extended data persistent (caching) support to alarm.
-Up persistent storage file version to 0.10 to cater for 2-level caching data.
-Cache alarm report even if event is 0 or 0xFE(unknown event). This is because we have a vision smoke detector which sends "unknown event" as a valid alarm report when triggered.
-Split Doxygen configuration files to CE and Portal versions.
-Updated Doxygen configuration files to include "search box" and installation instructions in the output.
-Added support for D-link mt7628 platform [EXPERIMENTAL, UNSUPPORTED].

Release version 4.09                                      Date: 16 Jun 2015
--------------------                                     -----------------
-Generalize the "hidden interface" implementation. If the interface is for whatever reason needs to be hidden from the client, setting the "IF_PROPTY_HIDDEN" property bit mask can achieve the result.
-For portal version only: remove all COMMAND_CLASS_FIRMWARE_UPDATE_MD for foreign (non-local) nodes, regardless endpoint number.

Release version 4.10                                      Date: 14 Jul 2015
--------------------                                     -----------------
-Added support for COMMAND_CLASS_CENTRAL_SCENE version 2.
-For sleeping device, when HCAPI receives wakeup notification, it will send all the commands in command queue,
 send supported get if there is no previous cached value, send report get for all the command classes in the node
 and endpoints to refresh cached values.

Release version 4.11                                      Date: 20 Jul 2015
--------------------                                     -----------------
-Fixed the longest key attribute row calculation error for COMMAND_CLASS_CENTRAL_SCENE v2.

Release version 4.12                                      Date: 18 Aug 2015
--------------------                                     -----------------
-Fixed TO# 06150 : ZIP Gateway discovery - Freeing uninitialized memory when no network interface has IP address assigned.
-Report callback for cached data will pass back the report_get input parameters when there is no cached data available.
-Added functionality in device database file to read alarm level with a range.
-Change report handler of V1 alarm report to perform range comparison against database.
-Convert certain V2 alarm events into doorlock reports. This can be disabled with macro. This is mainly done for
 Yale doorlock which sends alarm report instead of doorlock report during doorlock operation.
-Change visual studio solution file to be 2013 community version and added project file portal.

Release version 4.13                                      Date: 20 Aug 2015
--------------------                                     -----------------
-Allow alarm report to be processed even if no report callback setup is done since alarm report could be translated
 into doorlock report and client which only interested in doorlock report will not setup report callback.

Release version 4.14                                      Date: 24 Aug 2015
--------------------                                     -----------------
-Fixed TO # 06271
-For sensor cache get of COMMAND_CLASS_SENSOR_MULTILEVEL (version < 5), if type is non-zero, the cache will try to
 search a match in the cache.  If no cache is available, it will return timestamp=0 and the corresponding type in callback.

Release version 5.00                                      Date: 4 Sep 2015
--------------------                                     -----------------
-Replaced CYASSL with Openssl library version 1.0.1p.
-Supported "interpretation B" of supported termostat bitmasks in COMMAND_CLASS_THERMOSTAT_SETPOINT.

Release version 5.01                                      Date: 7 Oct 2015
--------------------                                     -----------------
-When calling zwnet_exit(), set flags to stop processing at various layers.
-Changed zwnet_exit() to release all "waiting to send Z/IP packets" threads from blocking,
 instead of waiting them to timeout.

Release version 5.02                                      Date: 19 Oct 2015
--------------------                                     -----------------
-Ensured user application is safe to call any API functions (except zwnet_init & zwnet_exit) in all type of callbacks
 (e.g. network node added/removed callback, node status callback, report callback from device, network status callback, etc).
-Added post-set polling for doorlock and multi-level switch.
-Save to persistent storage on network changes (i.e. add/remove nodes, node update, etc).
-Changed zwnet_exit() to wait for all threads to exit (no timeout) before freeing network resources.
-Implemented "background polling" -- 10 seconds polling of each "always on" device, 12 hours polling of "FLIR" device
 and on-wakeup polling of sleeping device.

Release version 5.03                                      Date: 9 Nov 2015
--------------------                                     -----------------
-DTLS listening socket binds to an IPv6 address that is route-able to Z/IP gateway.  This is to ensure
 that all the handshaking messages will use the same source IPv6 address.
-Changed demo apps to display context sensitive menu.
-Changed demo app "gw_discovery" to accept option: -4 for using IPv4; -6 for using IPv6; default to IPv6 without option given.
-For portal mode, changed the network error callback to be called from same callback thread as network operation notification.
-Added network resource reference count mechanism to prevent invalid access to freed memory if the callback thread is
 held by user application longer than the zwnet_exit can wait the callback thread to exit.
-Narrowed post-set polling for multi-level switch to GENERIC_TYPE_SWITCH_MULTILEVEL && SPECIFIC_TYPE_CLASS_A/B/C_MOTOR_CONTROL
-Allow all the supported_get functions to try to get cached/device database data regardless of command class version.
-Allow the insertion of JSON items before "device_records" in device database file.
-Removed the requirement to supply zwnet_init_t.node_info_file in zwnet_init() to store network and node info
 into persistent storage.  Instead, the directory path zwnet_init_t.net_info_dir is required.
-Removed zwnet_load() as this API has been merged to zwnet_init().
-Removed zwnet_save() as saving of network and node info into persistent storage is managed by ZWARE lib.
-Use the present of COMMAND_CLASS_SECURITY to determine whether the node is secure. Previously, the present of
 security command class marker (0xF100) was used.

Release version 6.00                                      Date: 24 Nov 2015
--------------------                                     -----------------
-Support Network Management Security 2 (add/remove node, remove/replace failed node and learn mode).
-Changed all network operations to have higher priority than background device polling.
-Modified demo apps to support Security 2.

Release version 6.01                                      Date: 10 Dec 2015
--------------------                                     -----------------
-Added functionality to convert alarm reports of any version to the same or different version of alarm reports
 of any value.
-Added functionality to convert basic_set to other reports (for now only support alarm report).
-Changed struct zwnetd_t to add controller capabilities.
-zwnet_get_desc() will return whether controller is capable to support security 2.
-Added S2 requested keys callback and zwnet_add_sec2_grant_key() for granting S2 keys.
-Changed zwnet_add() parameters.
-Added zwnet_sec2_get_dsk() API for getting Z/IP gateway DSK.  This is needed by the including controller
 when the Z/IP gateway joins other Z-wave network.
-Added s2_keys_valid and s2_grnt_keys fields in node descriptor to indicate granted keys to nodes in
 security 2 mode.
-Changed persistent storage to store s2_keys_valid and s2_grnt_keys fields in node descriptor.
-Modified demo app to support S2 add node.
-Modified device background polling to remove invalid node from polling list.

Release version 6.02                                      Date: 16 Dec 2015
--------------------                                     -----------------
-Z/IP gateway discovery will stop at about 2 seconds even if there are continuous MDNS packets received. This
 is to prevent DOS attack from rogue server.
-Fixed V2 alarm device cannot get supported events when cache is ON.
-Export the real_ver property of an interface to user application.
-Fixed demos add_node app to handle S2 inclusion failed case.

Release version 6.03                                      Date: 18 Dec 2015
--------------------                                     -----------------
-Fixed zwnet_add_sec2_accept() fails if rejecting (parameter accept=0) the DSK with invalid parameter dsk.

Release version 6.04                                      Date: 24 Dec 2015
--------------------                                     -----------------
-Fixed mdns gateway discovery invalid memory access.
-Fixed alarm report handling invalid memory access.
-Fixed device database loading of BASIC interface memcpy() with identical source and destination address.

Release version 6.05                                      Date: 29 Dec 2015
--------------------                                     -----------------
-Fixed execute NULL callback function in report handler due to allowing the basic_set and alarm command class
 to have NULL report callback function for the purpose of alarm to doorlock report conversion and basic_set handling.

Release version 6.06                                      Date: 6 Jan 2016
--------------------                                     -----------------
-Changed post-set polling intervals from fixed intervals to incremental intervals. For doorlock, the intervals
 are 1,2,3 seconds; for multi-level switch of SPECIFIC_TYPE_CLASS_A/B/C_MOTOR_CONTROL, the intervals are
 1,2,3,4.
-Added parameters to zwif_level_set() and zwif_dlck_op_set() to support post-set polling callback.

Release version 6.07                                      Date: 21 Jan 2016
--------------------                                     -----------------
-Supported Z/IP header extension option "encapsulation format info".
-Post-set will callback with reason ZWPSET_REASON_DEVICE_RMV when a node is removed.
-Get controller's DSK during initialization if it supports security 2. The DSK can be retrieved using zwnet_get_desc().
-Added checking of message compression offset in MDNS to make sure it points to a prior occurance of the same domain name.
-Disallow the use of host listening port 4123 when DTLS is used as transport mode.
-Added support for COMMAND_CLASS_SENSOR_ALARM.
-Added command redirection functionality specified by Device database. (When a command is received from ZWave,
 the source endpoint address can be changed and re-routed to Root device/another endpoint as specified in device database).

Release version 6.08                                      Date: 26 Jan 2016
--------------------                                     -----------------
-Added Alarm sensor report to Notification report conversion function in Device DB.
-Device DB: Fibaro flood sensor --
  a.Set only group 1 & 2 during inclusion.
  b.Redirect both Basic Set and Alarm sensor report to Endpoint 0
  c.Add in configuration param so device will send Alarm sensor report instead of Basic Set.
  d.Convert both Basic Set and Alarm sensor report to Notification report.
-Fixed Notification report param length 0 after conversion issue.
-Fixed 'fake' interface will be created in all endpoints of the Node issue.
-Post set polling supports Specific device class SPECIFIC_TYPE_MOTOR_MULTIPOSITION for generic device class
 GENERIC_TYPE_SWITCH_MULTILEVEL

Release version 6.09                                      Date: 29 Jan 2016
--------------------                                      -----------------
-Fixed gateway discovery using the broadcast node_info_get method can't accept response with Z/IP header extension.

Release version 6.10                                      Date: 1 Feb 2016
--------------------                                      -----------------
-Allow sensor alarm report to be processed even if no callback function so that alarm report can be generated
 if instructed by device DB.

Release version 7.00                                      Date: 24 Feb 2016
--------------------                                      -----------------
-Use mailbox ACK as indication of Wakeup Notification in order to support "dual-Z/IP client" in a system setup.
-Supported COMMAND_CLASS_SENSOR_MULTILEVEL version 9
-Supported COMMAND_CLASS_ALARM/COMMAND_CLASS_NOTIFICATION version 7
-Fixed background device polling causes "firmware update md get" command not processed during firmware update.
-Changed maximum queue length for pendig TLS connections to 128.
-Changed zwif_xxx_get 'flag' parameter to require either ZWIF_GET_BMSK_CACHE or ZWIF_GET_BMSK_LIVE or both for
 getting cache only report, live report through Z-wave or both respectively.
-Changed report callback only on condition there is a change from cached value.  Time-stamp will be updated
 regardless of report content.
-Updated to openssl library version 1.0.1r.
-Removed assumption that all alarm devices will be listed in device database.

Release version 7.01                                      Date: 1 Mar 2016
--------------------                                      -----------------
-Fixed bug if there is a sleeping node in the network, network update will terminate at the sleeping node
 and not proceed further.
-Delay query detailed node info of sleeping nodes until they are awake in the following situations:
 o Initialization
 o Network update
 o Node update if the sleeping node is sleeping.

Release version 7.02                                      Date: 16 Mar 2016
--------------------                                      -----------------
-Added script file to generate doxygen documentation in Ubuntu Linux
-Modified Readme.txt for doxygen documentation
-Added definition of DEV_ID_TYPE_RANDOM for device id type in "manufacturer specific command class"->
 "device specific report".
-Changed background device polling and wakeup notification polling to be carried out only after node
 info state-machine has completed.
-Modified INSTALL and Makefile to support conditional build of single or multiple clients version that
 can be connected to a same Z/IP gateway. Added make option is SINGLE_CLIENT=1.

Release version 7.03                                      Date: 7 Apr 2016
--------------------                                      -----------------
-zwif_xxx_set() will update cache value and generate report callback on condition there is
 a change from cached value.
-Handle APPLICATION_BUSY from Z/IP gateway.
-Reverted INSTALL and Makefile to version 6.10 as single or multiple clients selection is
 done through initialization parameter in zwnet_init().
-Fixed using wrong time-out handler while waiting for Association Group Info Report.
-In DTLS read thread, changed the select() polling loop to handle return error,
 instead of just quitting the loop.
-TLS server thread, changed the select() polling loop to handle return error,
 instead of just quitting the loop.
-Updated cjson.c and cjson.h
-Added device database entry to indicate which alarm CC interface does not send event clear report.

Release version 7.04                                      Date: 11 Apr 2016
--------------------                                      -----------------
-Auto-select single or multiple clients configuration based on Z/IP gateway version.
-Added device database entry to indicate which binary sensor CC interface does not send event clear report.
-Check that OpenSSL library is configured with thread support.

Release version 7.05                                      Date: 4 May 2016
--------------------                                      -----------------
-Upgraded openssl to LTS version 1.0.2 (see https://www.openssl.org/policies/releasestrat.html)
 which will be supported until 2019-12-31.
-Changed zwif_alrm_get_poll() and zwif_alrm_get() APIs parameter 'vtype' to accept -1 to indicate "don't care" for cache get.
-Support multihomed IP addresses for both DTLS and unencrypted connections.

Release version 7.06                                      Date: 11 May 2016
--------------------                                      -----------------
-Fixed TO# 06986 - a successful Cached Get call that returned ZW_ERR_CACHE_AVAIL was not followed up by
 callback after a node was removed. The fix will callback to user application with indication of invalid
 content.

Release version 7.10                                      Date: 1 Jul 2016
--------------------                                      -----------------
-Support COMMAND_CLASS_BARRIER_OPERATOR.
-Up persistent storage file version to 0.12 for storing node->listen.
-Added check for "listening" flag in node information frame (NIF) before a device is added to the list for device
 background polling.
-Fixed TO# 07006: "'Battery Get' executed every 10 secs" by not polling device with "listening" flag not set in NIF.
-Support multi-channel command class version 4
-Do not save network information to persistent storage when network initialization failed.
-Check for device with support of "wake up command class" and non-listening (as in NIF) before qualifying it as
 sleeping device. All sleeping devices will have command buffering/mailbox enabled.
-Fixed TO# 07101: "Z-ware does not support AOS nodes implementing wakeup CC".


Release version 7.20                                      Date: 4 Aug 2016
--------------------                                      -----------------
-Support S2 Client Side Authentication (CSA).
-Support proxy inclusion for zipgateway that supports "inclusion controller command class".
-Removed net->net_desc.dsk as the gateway DSK is no longer constant, it changes when new node is added.
-Modified cache set through report callback without holding network lock.
-Use Z/IP "encapsulation format info" extension to control which security encapsulation to use for outgoing packets
 as Z/IP gateway version 2.59 has deprecated the "secure origin" bit in Z/IP header. Likewise, the extension is used
 to determine whether incoming packets are secure.
-Remove dsk from struct zwnetd_t as Z/IP gateway DSK is not constant over time. Use zwnet_sec2_get_dsk() instead to get
 the latest Z/IP gateway DSK.
-Added parameters to zwnet_initiate() for reported Z/IP gateway latest DSK.  The DSK is needed for joining into a S2
 enable Z-wave network.

Release version 7.21                                      Date: 12 Aug 2016
--------------------                                      -----------------
-Extend the use Z/IP "encapsulation format info" extension to Z/IP gateway version 2.58 and 2.25 and above.
-Control CRC-16 encapsulation through Z/IP "encapsulation format info" extension.

Release version 7.22                                      Date: 17 Aug 2016
--------------------                                      -----------------
-Added parameter 'incl_on_behalf' to zwnet_add() to support inclusion on-behalf (i.e. adding node using
 a inclusion controller rather than directly by Z/IP gateway).

Release version 7.23                                      Date: 23 Aug 2016
--------------------                                      -----------------
-Support unsolicited inclusion on-behalf.
-Support user initiated and unsolicited "replace failed node on-behalf".
-Always set controller's listen flag to 1.
-Change Association Group support handling to ignore incoming Association command if the Group ID doesn't match the supported Group ID.

Release version 7.24                                      Date: 23 Aug 2016
--------------------                                      -----------------
-Fixed TO#7277. zwif_group_add() with parameter 'cnt' equals to zero will now return error ZW_ERR_VALUE instead of zero.
-Changed zwnet_fail() API so that "replace failed node on-behalf" doesn't require failed node id as input.

Release version 7.25                                      Date: 16 Sep 2016
--------------------                                      -----------------
-Clear association group member of group 1 (Lifeline) after Z/IP gateway is reset, joining or leaving a Z-wave network.
-Extend DTLS keep alive to cater for CSA key entry timeout of 4 minutes.
-Wait forever all threads in timer and transport layer before exiting the Z-ware C library.
-Fixed when inclusion a node with large number of endpoints (e.g. 30), if device polling of endpoint timeout right after
 node info cached report is received, it will crash.
-Use supervision command class to replace post-set polling if supported by the device.
-Get S2 grant keys from node info cached report if Z/IP gateway's COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY is version 2 and above.
-Support color switch cc version 1
-Upgraded COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION support to version 3

Release version 7.30                                      Date: 18 Oct 2016
--------------------                                      -----------------
-Support conversion of binary sensor to alarm interface through device database.
-Added "hidden" entry in JSON device database to support hiding of interface from user application.
-If binary sensor is not capable to support event clear, disable polling of binary sensor report and auto-generate
 event clear based on the "event_clear_timeout" entry in device database or default to 5 minutes if the entry is missing.
-If alarm is not capable to support event clear, auto-generate event clear based on the "event_clear_timeout" entry in device database.
-Fixed TO#07332 by blocking node alive status callback before call to zwnet_init() has completed.
-Fixed mis-interpretation of COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION->NODE_ADD_DSK_REPORT_V2's "input dsk length" field
 as length of DSK keys.
-Updated to openssl library version 1.0.2j.

Release version 7.31                                      Date: 25 Oct 2016
--------------------                                      -----------------
-Polling of mailbox intermediate status at 1 minute interval to determine whether a mailbox message has been dropped.
-Support conversion of basic set, alarm sensor and  binary sensor to existing alarm interface through device database.
-Changed firmware update minimum fragment size to 10 to support S2 sample app firmware update.

Release version 7.32                                      Date: 18 Nov 2016
--------------------                                      -----------------
-Fixed TO# 07454 - Z-ware does not answer as asked on multicomand encapsulation
-Set linelife group and wakeup notification receiving node id to SIS or Primary controller id.
-Support "network management proxy' command class version 2 which allows querying of endpoint info. Note: Only Z/IP gateway
 version 2.60 and above is supported, see the #define ZWNET_ZIPGW_VER_EP_NIF in zip_api_pte.h
-Handle multi-command encapsulation from a node and reply on behalf of Z/IP gateway for encapsulated report_get command.
-Support COMMAND_CLASS_SWITCH_COLOR version 2 and 3.
-Revert back the way to determine a node is securely included to a Z-wave network by using the presence of security supported
 command class in node/endpoint.
-Added member "propty" in node descriptor to mark whether the node is security capable (bit-mask NODE_PROPTY_SECURE_CAP_S0 or/and
 NODE_PROPTY_SECURE_CAP_S2), included securely (bit-mask NODE_PROPTY_INC_SECURE) or insecurely (bit-mask NODE_PROPTY_INC_UNSECURE).
 Application must use the combination of NODE_PROPTY_SECURE_CAP_S0/2 and  NODE_PROPTY_INC_UNSECURE to display a warning message
 to user about the node is security capable but included insecurely.
-Support DSK get/report for learn mode/inclusion mode.
-Support OTA firmware update in HEX file format. Note that the file name must have extension ".hex" for hex file; all other
 extension will be treated as binary file.
-Set all Z/IP gateway interfaces as secure to force Z/IP packet secure origin bit set to 1.

Release version 7.33                                      Date: 16 Dec 2016
--------------------                                      -----------------
-Make the presence of "End of File" record in hex file optional for firmware update.
-Support alarm sensor "no event clear" in device database.
-Z/IP packet for Z/IP gateway always set "secure origin" bit to 1 without encapsulation header.
-Check value of basic set to ensure it is between 0 and 99, or 255.
-Support learn mode status LEARN_MODE_INTERVIEW_COMPLETED.  Delay running initialization state-machine until the
 LEARN_MODE_INTERVIEW_COMPLETED is received to avoid clashing with Z/IP gateway probing.
-Support Pre-kit demo [Not fully tested].
-The notification callback to report network operation status,zwnet_notify_fn() is expanded to include additional info.

Release version 7.34                                      Date: 22 Dec 2016
--------------------                                      -----------------
-Register supported_get() callback function to the interface regardless of parameter 'cahce' value.
-Added entries in file INSTALL, build_openssl_lib.sh and Makefile to support Raspberry Pi.
-Added zwif_thrmo_md_sup_cache_get() to get supported thermostat modes from cache directly.

Release version 7.35                                      Date: 10 Jan 2017
--------------------                                      -----------------
-Added support for thermostat setpoint version 3
-Added supported thermostat setpoint temparature range in device db for thermostat setpoint version 1 & 2.
-Added zwif_alrm_sup_cache_get(), zwif_alrm_sup_evt_cache_get(), zwif_barrier_notif_sup_cache_get(), zwif_color_sw_sup_cache_get(),
 zwif_level_sup_cache_get(), zwif_meter_sup_cache_get(), zwif_sensor_unit_cache_get(), zwif_thrmo_fan_md_sup_cache_get(),
 zwif_thrmo_md_sup_cache_get(), zwif_thrmo_setp_sup_cache_get(), zwif_thrmo_setp_sup_range_cache_get(),
 zwif_usrcod_sup_cache_get() APIs to get cache values directly.
-Modified zwif_alrm_snsr_sup_cache_get(), zwif_bsensor_sup_cache_get(), zwif_sensor_sup_cache_get() parameters to use caller supplied buffer for output.
-Workaround for Z/IP gateway version 2.25 where node add status sometimes contains no command class.  This causes Z-ware lib wrongly set
 the newly added node as included unsecurely.
-Fixed TO# 07616 with the above-mentioned workaround.
-Removed background device polling for basic CC.
-Delete old network info persistent storage file immediately after learn mode status done or reset status done was received.
-Removed the query of version for extra command classes set by Zware-lib during initialization.

Release version 7.36                                      Date: 13 Feb 2017
--------------------                                      -----------------
-Upgraded COMMAND_CLASS_THERMOSTAT_FAN_STATE to version 2
-Upgraded COMMAND_CLASS_ASSOCIATION_GRP_INFO to version 3
-Upgraded COMMAND_CLASS_CENTRAL_SCENE to version 3
-Upgraded COMMAND_CLASS_FIRMWARE_UPDATE_MD to version 5
-Removed the requirement to provide firmware file checksum and fragment size for firmware update.
-If Z/IP gateway is a secondary controller, reset default operation will not send DEVICE_RESET_LOCALLY_NOTIFICATION to primary controller.
-Changed Z/IP frame timeout value from 6.4 s to 2.5 s.
-Fixed TO# 07282 : Z-ware cannot reset Z/IP gateway in non-SIS network
-Augmented firmware update to network operation ZWNET_OP_FW_UPDT.  Provide progress percentage of the firmware update to user application
 through ZWNET_OP_FW_UPDT->OP_FW_UPLOADING notification.
-Turn off background device polling while running firmware update.
-Retry sending report get commands if report receiving timeouts in node and controller info state-machines.
-Allow default wakeup interval to be configured on all sleeping (wakeup CC) devices after inclusion, using the device
 database "global_settings"->"wakeup_interval".
-Added device global settings in struct dev_cfg_usr_t for device database managed by user application.

Release version 7.37                                      Date: 13 Mar 2017
--------------------                                      -----------------
-Workaround Valgrind issue on Beaglebone Black platform for post-set-poll (Window covering device) and no_event generation
 of alarm device that makes use of device DB "no_event_clear" setting.
-4-byte aligned some structure place holder for compability with ARMV5 based processor.
-Fixed TO# 07697 - unaligned memory access issue on armv5.
-Created new node status of "sleeping" beside "up" and "down".
-Changed add-on command classes to Z/IP gateway node info, which are COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_ASSOCIATION and
 COMMAND_CLASS_MULTI_CMD, to unsecure only.
-Extend the FIRMWARE_UPDATE_MD_REQUEST_REPORT timeout to 26 seconds to cater for unresponsive device in heavy network condition.
-Workaround for Z/IP gateway secure only interface sends Z/IP packet to Z/IP client with encapsulation header extension indicated as insecure.
 It is assumed all Z/IP packets originated from Z/IP gateway are secure even if the encapsulation header extension indicates otherwise.
-Check before setting of controller id to Lifeline group; if target node doesn't support multi-channel, use node only association.
-Temporary enable sending of "Wakeup no more info" in multi-client mode, until Z/IP gateway implements timer
 for sending "Wakeup no more info" after a sleeping node was included.
-Modified openssl library (patch file "openssl.patch") to allow resend of DTLS "close notify" to peer for graceful DTLS connection shutdown.
-Determination of whether a node is securely included will make use of S2 granted keys if available.
-Moved sending of "device reset locally" into reset state-machine to fix the issue of unable to send "default set" to
 Z/IP gateway when the SIS is down.
-Changed resend of Z/IP packet to use existing DTLS session instead of closing the existing DTLS session and create a new one.
-When adding a node/endpoint into an association group, check for the node/endpoint supports the command classes sent by the
 association group if association group info (AGI) is available.
-When adding a node/endpoint into an association group, if the target device does not support multi-channel but supports
 multi-channel association, use node only association.
-Treat file extension ".hex", ".ota" and ".otz" as HEX file in OTA firmware update.

Release version 7.38                                      Date: 20 Mar 2017
--------------------                                      -----------------
-Fixed when excluded from SIS, does not set NODE_PROPTY_ADD_SECURE in controller node property.
-Added controller Z-wave role in network descriptor structure zwnetd_t. The descriptor is returned through zwnet_get_desc().
-Support learn mode resulting in controller replication or controller shift in security 2 (S2).
-If the included node is of Role Type Portable Slave (PS), set the wakeup interval to zero.
-Fixed node info state-machine goes into endless loop if device responds with node naming name report when asked with
 node naming location get.
-Fixed zwif_fw_updt_req returns ZW_ERR_NONE instead of ZW_ERR_QUEUED for FLIRS device.
-Fixed central scence controller supported report version 1 caused Z-ware lib to crash.
-Added demo program bin_sensor to demonstrate setting up of unsolicited address and receiving unsolicited report.

