#include <stdio.h>
#include "zwcontrol_api.h"
#include <unistd.h>

#define  DESC_TYPE_NODE     1
#define  DESC_TYPE_EP       2
#define  DESC_TYPE_INTF     3

ResCallBack resCallBack = NULL;

const char *sensor_type_str[] =
        {
                "undefined",
                "Temperature sensor",
                "General purpose sensor",
                "Luminance sensor",
                "Power sensor",
                "Relative humidity sensor",
                "Velocity sensor",
                "Direction sensor",
                "Atmospheric pressure sensor",
                "Barometric pressure sensor",
                "Solar radiation sensor",
                "Dew point sensor",
                "Rain rate sensor",
                "Tide level sensor",
                "Weight sensor",
                "Voltage sensor",
                "Current sensor",
                "CO2-level sensor",
                "Air flow sensor",
                "Tank capacity sensor",
                "Distance sensor",
                "Angle Position sensor",
                "Rotation sensor",
                "Water temperature sensor",
                "Soil temperature sensor",
                "Seismic intensity sensor",
                "Seismic magnitude sensor",
                "Ultraviolet sensor",
                "Electrical resistivity sensor",
                "Electrical conductivity sensor",
                "Loudness sensor",
                "Moisture sensor",
                "Frequency sensor",
                "Time sensor",
                "Target temperature sensor"
        };

const char *sensor_unit_str[] =
        {
                NULL,
                NULL,
                NULL,
                NULL,
                "Celsius (C)",    //Air temperature unit
                "Fahrenheit (F)",
                NULL,
                NULL,
                "Percentage",   //General purpose unit
                "Dimensionless value",
                NULL,
                NULL,
                "Percentage",   //Luminance unit
                "Lux",
                NULL,
                NULL,
                "W",      //Power unit
                "Btu/h",
                NULL,
                NULL,
                "Percentage",   //Relative humidity unit
                "g/m3",
                NULL,
                NULL,
                "m/s",      //Velocity unit
                "mph",
                NULL,
                NULL,
                "degrees",      //Direction unit
                NULL,
                NULL,
                NULL,
                "kPa",        //Atmospheric pressure unit
                "inches of Mercury",
                NULL,
                NULL,
                "kPa",        //Barometric pressure unit
                "inches of Mercury",
                NULL,
                NULL,
                "W/m2",         //Solar radiation unit
                NULL,
                NULL,
                NULL,
                "Celsius (C)",    //Dew point unit
                "Fahrenheit (F)",
                NULL,
                NULL,
                "mm/h",   //Rain rate unit
                "in/h",
                NULL,
                NULL,
                "m",      //Tide level unit
                "feet",
                NULL,
                NULL,
                "kg",       //Weight unit
                "pounds",
                NULL,
                NULL,
                "V",      //Voltage unit
                "mV",
                NULL,
                NULL,
                "A",   //Current unit
                "mA",
                NULL,
                NULL,
                "ppm",    //CO2-level unit
                NULL,
                NULL,
                NULL,
                "m3/h",   //Air flow unit
                "cfm",
                NULL,
                NULL,
                "l",    //Tank capacity unit
                "cbm",
                "US gallons",
                NULL,
                "m",      //Distance unit
                "cm",
                "feet",
                NULL,
                "Percentage",     //Angle Position unit
                "Degrees rel. to north pole",
                "Degrees rel. to south pole",
                NULL,
                "rpm",   //Rotation unit
                "Hz",
                NULL,
                NULL,
                "Celsius (C)",    //Water temperature unit
                "Fahrenheit (F)",
                NULL,
                NULL,
                "Celsius (C)",    //Soil temperature unit
                "Fahrenheit (F)",
                NULL,
                NULL,
                "Mercalli",       //Seismic intensity unit
                "European Macroseismic",
                "Liedu",
                "Shindo",
                "Local (ML)",     //Seismic magnitude unit
                "Moment (MW)",
                "Surface wave (MS)",
                "Body wave (MB)",
                "UV index",    //Ultraviolet unit
                NULL,
                NULL,
                NULL,
                "ohm metre",    //Electrical resistivity unit
                NULL,
                NULL,
                NULL,
                "siemens per metre",    //Electrical conductivity unit
                NULL,
                NULL,
                NULL,
                "Absolute loudness (dB)", //Loudness unit
                "A-weighted decibels (dBA)",
                NULL,
                NULL,
                "Percentage", //Moisture unit
                "Volume water content (m3/m3)",
                "Impedance (k ohm)",
                "Water activity (aw)",
                "Hertz (Hz)",           //Frequency unit
                "Kilo Hertz (KHz)",
                NULL,
                NULL,
                "Second (s)",           //Time unit
                NULL,
                NULL,
                NULL,
                "Celsius (C)",          //Target temperature unit
                "Fahrenheit (F)",
                NULL,
                NULL
        };

const char *dev_category_str[] =
        {
                "undefined",
                "Sensor alarm",
                "On/off switch",
                "Power strip",
                "Siren",
                "Valve",    //5
                "Simple display",
                "Door lock with keypad",
                "Sub energy meter",
                "Advanced whole home energy meter",
                "Simple whole home energy meter",   //10
                "Sensor",
                "Light dimmer switch",
                "Window covering no position/endpoint",
                "Window covering end point aware",
                "Window covering position/end point aware", //15
                "Fan switch",
                "Remote control - multipurpose",
                "Remote control - AV",
                "Remote control - simple",
                "Gateway (unrecognized by client)", //20
                "Central controller",
                "Set top box",
                "TV",
                "Sub system controller",
                "Gateway",  //25
                "Thermostat - HVAC",
                "Thermostat - setback",
                "Wall controller"
        };

const char *power_level_ind_str[] =
{
    "Normal power",
    "minus 1dBm",
    "minus 2dBm",
    "minus 3dBm",
    "minus 4dBm",
    "minus 5dBm",
    "minus 6dBm",
    "minus 7dBm",
    "minus 8dBm",
    "minus 9dBm"
};

/**
hl_bin2str - convert binary string to hex string
@param[in]  buf      The buffer that stores the binary string
@param[in]  len      The length of the binary string.
@param[out] hex_str  The buffer that stores the output string.
@param[in]  hex_str_len      The length of the output buffer.
@return
*/
void hl_bin2str(void * buf, uint32_t len, char *hex_str, uint32_t hex_str_len)
{
    uint8_t     *bin_byte = (uint8_t *)buf;
    char        tmp[8];

    hex_str[0] = '\0';

    //Convert a line of binary data into hex string
    while (len-- > 0)
    {
        sprintf(tmp,"%02X ",(unsigned) *bin_byte++);
        strcat(hex_str, tmp);
    }
}

/**
hl_class_str_get - Get command class string
@param[in]	cls	        class
@param[in]	ver	        version of the command class
@return		Command class string if found, else return string "UNKNOWN"
*/
static char *hl_class_str_get(uint16_t cls, uint8_t ver)
{
    switch (cls)
    {
            case COMMAND_CLASS_BASIC:
            {
                return "COMMAND_CLASS_BASIC";
            }
            break;

            case COMMAND_CLASS_SWITCH_MULTILEVEL:
            {
                return "COMMAND_CLASS_SWITCH_MULTILEVEL";
            }
            break;

            case COMMAND_CLASS_SWITCH_BINARY:
            {
                return "COMMAND_CLASS_SWITCH_BINARY";
            }
            break;

            case COMMAND_CLASS_SWITCH_ALL:
            {
                return "COMMAND_CLASS_SWITCH_ALL";
            }
            break;

            case COMMAND_CLASS_MANUFACTURER_SPECIFIC:
            {
                return "COMMAND_CLASS_MANUFACTURER_SPECIFIC";
            }
            break;

            case COMMAND_CLASS_VERSION:
            {
                return "COMMAND_CLASS_VERSION";
            }
            break;

            case COMMAND_CLASS_POWERLEVEL:
            {
                return "COMMAND_CLASS_POWERLEVEL";
            }
            break;

            case COMMAND_CLASS_CONTROLLER_REPLICATION:
            {
                return "COMMAND_CLASS_CONTROLLER_REPLICATION";
            }
            break;

            case COMMAND_CLASS_NODE_NAMING:
            {
                return "COMMAND_CLASS_NODE_NAMING";
            }
            break;

            case COMMAND_CLASS_SENSOR_BINARY:
            {
                return "COMMAND_CLASS_SENSOR_BINARY";
            }
            break;

            case COMMAND_CLASS_SENSOR_MULTILEVEL:
            {
                return "COMMAND_CLASS_SENSOR_MULTILEVEL";
            }
            break;

            case COMMAND_CLASS_ASSOCIATION:
            {
                return "COMMAND_CLASS_ASSOCIATION";
            }
            break;

            case COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2:
            {
                if (ver >= 2)
                {
                    return "COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION";
                }
                return "COMMAND_CLASS_MULTI_INSTANCE_ASSOCIATION";
            }
            break;

            case COMMAND_CLASS_ASSOCIATION_COMMAND_CONFIGURATION:
            {
                return "COMMAND_CLASS_ASSOCIATION_COMMAND_CONFIGURATION";
            }
            break;

            case COMMAND_CLASS_NO_OPERATION:
            {
                return "COMMAND_CLASS_NO_OPERATION";
            }
            break;

            case COMMAND_CLASS_MULTI_CHANNEL_V2:
            {
                if (ver >= 2)
                {
                    return "COMMAND_CLASS_MULTI_CHANNEL";
                }
                return "COMMAND_CLASS_MULTI_INSTANCE";
            }
            break;

            case COMMAND_CLASS_WAKE_UP:
            {
                return "COMMAND_CLASS_WAKE_UP";
            }
            break;

            case COMMAND_CLASS_MANUFACTURER_PROPRIETARY:
            {
                return "COMMAND_CLASS_MANUFACTURER_PROPRIETARY";
            }
            break;

            case COMMAND_CLASS_METER_TBL_MONITOR:
            {
                return "COMMAND_CLASS_METER_TBL_MONITOR";
            }
            break;

            case COMMAND_CLASS_METER_TBL_CONFIG:
            {
                return "COMMAND_CLASS_METER_TBL_CONFIG";
            }
            break;

            case COMMAND_CLASS_METER:
            {
                return "COMMAND_CLASS_METER";
            }
            break;

            case COMMAND_CLASS_METER_PULSE:
            {
                return "COMMAND_CLASS_METER_PULSE";
            }
            break;

            case COMMAND_CLASS_SIMPLE_AV_CONTROL:
            {
                return "COMMAND_CLASS_SIMPLE_AV_CONTROL";
            }
            break;

            case COMMAND_CLASS_CONFIGURATION:
            {
                return "COMMAND_CLASS_CONFIGURATION";
            }
            break;

            case COMMAND_CLASS_INDICATOR:
            {
                return "COMMAND_CLASS_INDICATOR";
            }
            break;

            case COMMAND_CLASS_SECURITY:
            {
                return "COMMAND_CLASS_SECURITY";
            }
            break;

            case COMMAND_CLASS_HAIL:
            {
                return "COMMAND_CLASS_HAIL";
            }
            break;

            case COMMAND_CLASS_PROTECTION:
            {
                return "COMMAND_CLASS_PROTECTION";
            }
            break;

            case COMMAND_CLASS_SWITCH_TOGGLE_BINARY:
            {
                return "COMMAND_CLASS_SWITCH_TOGGLE_BINARY";
            }
            break;

            case COMMAND_CLASS_BATTERY:
            {
                return "COMMAND_CLASS_BATTERY";
            }
            break;

            case COMMAND_CLASS_DOOR_LOCK:
            {
                return "COMMAND_CLASS_DOOR_LOCK";
            }
            break;

            case COMMAND_CLASS_USER_CODE:
            {
                return "COMMAND_CLASS_USER_CODE";
            }
            break;

            case COMMAND_CLASS_ALARM:
            {
                if (ver >= 3)
                {
                    return "COMMAND_CLASS_NOTIFICATION";
                }
                return "COMMAND_CLASS_ALARM";
            }
            break;

            case COMMAND_CLASS_SCHEDULE_ENTRY_LOCK:
            {
                return "COMMAND_CLASS_SCHEDULE_ENTRY_LOCK";
            }
            break;

            case COMMAND_CLASS_DOOR_LOCK_LOGGING:
            {
                return "COMMAND_CLASS_DOOR_LOCK_LOGGING";
            }
            break;

            case COMMAND_CLASS_TIME_PARAMETERS:
            {
                return "COMMAND_CLASS_TIME_PARAMETERS";
            }
            break;

            case COMMAND_CLASS_CRC_16_ENCAP:
            {
                return "COMMAND_CLASS_CRC_16_ENCAP";
            }
            break;

            case COMMAND_CLASS_TRANSPORT_SERVICE:
            {
                return "COMMAND_CLASS_TRANSPORT_SERVICE";
            }
            break;

            case COMMAND_CLASS_ZIP:
            {
                return "COMMAND_CLASS_ZIP";
            }
            break;

            case COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY:
            {
                return "COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY";
            }
            break;

            case COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION:
            {
                return "COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION";
            }
            break;

            case COMMAND_CLASS_NETWORK_MANAGEMENT_BASIC:
            {
                return "COMMAND_CLASS_NETWORK_MANAGEMENT_BASIC";
            }
            break;

            case COMMAND_CLASS_NETWORK_MANAGEMENT_PRIMARY:
            {
                return "COMMAND_CLASS_NETWORK_MANAGEMENT_PRIMARY";
            }
            break;

            case COMMAND_CLASS_THERMOSTAT_FAN_MODE:
            {
                return "COMMAND_CLASS_THERMOSTAT_FAN_MODE";
            }
            break;

            case COMMAND_CLASS_THERMOSTAT_FAN_STATE:
            {
                return "COMMAND_CLASS_THERMOSTAT_FAN_STATE";
            }
            break;

            case COMMAND_CLASS_THERMOSTAT_MODE:
            {
                return "COMMAND_CLASS_THERMOSTAT_MODE";
            }
            break;

            case COMMAND_CLASS_THERMOSTAT_OPERATING_STATE:
            {
                return "COMMAND_CLASS_THERMOSTAT_OPERATING_STATE";
            }
            break;

            case COMMAND_CLASS_THERMOSTAT_SETPOINT:
            {
                return "COMMAND_CLASS_THERMOSTAT_SETPOINT";
            }
            break;

            case COMMAND_CLASS_THERMOSTAT_SETBACK:
            {
                return "COMMAND_CLASS_THERMOSTAT_SETBACK";
            }
            break;

            case COMMAND_CLASS_CLOCK:
            {
                return "COMMAND_CLASS_CLOCK";
            }
            break;

            case COMMAND_CLASS_LOCK:
            {
                return "COMMAND_CLASS_LOCK";
            }
            break;

            case COMMAND_CLASS_CLIMATE_CONTROL_SCHEDULE:
            {
                return "COMMAND_CLASS_CLIMATE_CONTROL_SCHEDULE";
            }
            break;

            case COMMAND_CLASS_MULTI_CMD:
            {
                return "COMMAND_CLASS_MULTI_CMD";
            }
            break;

            case COMMAND_CLASS_APPLICATION_STATUS:
            {
                return "COMMAND_CLASS_APPLICATION_STATUS";
            }
            break;

            case COMMAND_CLASS_FIRMWARE_UPDATE_MD:
            {
                return "COMMAND_CLASS_FIRMWARE_UPDATE_MD";
            }
            break;

            case COMMAND_CLASS_ZWAVEPLUS_INFO:
            {
                return "COMMAND_CLASS_ZWAVEPLUS_INFO";
            }
            break;

            case COMMAND_CLASS_DEVICE_RESET_LOCALLY:
            {
                return "COMMAND_CLASS_DEVICE_RESET_LOCALLY";
            }
            break;

            case COMMAND_CLASS_ASSOCIATION_GRP_INFO:
            {
                return "COMMAND_CLASS_ASSOCIATION_GRP_INFO";
            }
            break;

            case COMMAND_CLASS_SCENE_ACTIVATION:
            {
                return "COMMAND_CLASS_SCENE_ACTIVATION";
            }
            break;

            case COMMAND_CLASS_SCENE_ACTUATOR_CONF:
            {
                return "COMMAND_CLASS_SCENE_ACTUATOR_CONF";
            }
            break;

            case COMMAND_CLASS_SCENE_CONTROLLER_CONF:
            {
                return "COMMAND_CLASS_SCENE_CONTROLLER_CONF";
            }
            break;

            case COMMAND_CLASS_ZIP_GATEWAY:
            {
                return "COMMAND_CLASS_ZIP_GATEWAY";
            }
            break;

            case COMMAND_CLASS_ZIP_PORTAL:
            {
                return "COMMAND_CLASS_ZIP_PORTAL";
            }
#ifdef  TEST_EXT_CMD_CLASS
            case COMMAND_CLASS_EXT_TEST:        //Testing of extended command class
            {
                return "COMMAND_CLASS_EXT_TEST";
            }
            break;
#endif
        default:
            return "UNKNOWN";
    }
}

/**
hl_field_get - get a field separated by comma
@param[in]  rec_str     Record string, will be modified
@return  Field on success, NULL if there is no more field.
*/
static char *hl_field_get(char **rec_str)
{
    char *s;
    char *field;
    char c;

    s = *rec_str;

    if (s == NULL)
        return(NULL);

    field = s;

    while (1)
    {
        c = *s++;
        if ((c == ',') || (c == '\0'))
        {
            if (c == '\0')
            {
                s = NULL;
            }
            else
                s[-1] = '\0';

            *rec_str = s;
            return(field);
        }
    }
}

/**
hl_file_ver_get - scan file for version number
@param[in]  file        file descriptor
@param[out] ver         version
@return  Non-zero on success; zero on failure.
*/
static int hl_file_ver_get(FILE *file, uint16_t *ver)
{
    static const char   ver_delimiters[] = " =,\r\n";
    char                line[180];
    char				*prm_name;
    char				*prm_val;

    while (fgets(line, 180, file))
    {
        if (*line == '#')
        {   //Skip comment line
            continue;
        }

        //Check if '=' exists
        if (strchr(line, '='))
        {
#ifdef USE_SAFE_VERSION
            char *next_token;
            //Get the parameter name and value
            prm_name = strtok_s(line, ver_delimiters, &next_token);
#else
            //Get the parameter name and value
            prm_name = strtok(line, ver_delimiters);
#endif

            if (prm_name)
            {
#ifdef USE_SAFE_VERSION
                prm_val = strtok_s(NULL, ver_delimiters, &next_token);
#else
                prm_val = strtok(NULL, ver_delimiters);
#endif

                if (!prm_val)
                {
                    continue;
                }

                //Compare the parameter name
                if (strcmp(prm_name, "ver") == 0)
                {
                    unsigned version;
#ifdef USE_SAFE_VERSION
                    if (sscanf_s(prm_val, "%u", &version) == 1)
                    {
                        *ver = (uint32_t)version;
                        return 1;
                    }
#else
                    if (sscanf(prm_val, "%u", &version) == 1)
                    {
                        *ver = (uint16_t)version;
                        return 1;
                    }
#endif
                }
            }
        }
    }

    return 0;
}

/**
hl_dev_setting_get - get device specific settings
@param[in]  file        File descriptor
@param[out] dev_cfg     Device specific settings.
@return  Zero on success, non-zero on failure.
@post Caller is required to free the allocate buffer in dev_cfg->dev_cfg_buf
      if return value equals to zero and dev_cfg->dev_cfg_cnt > 0.
*/
static int hl_dev_setting_get(FILE *file, dev_spec_cfg_t *dev_cfg)
{
    char line[256];
    long int file_pos;
    dev_rec_t *rec_buf;
    char *field;
    char *rec_str;
    unsigned vid;
    unsigned ptype;
    unsigned pid;
    unsigned category;
    unsigned wkup_intv;
    unsigned grp_id;
    unsigned cfg_prm_num;
    unsigned cfg_prm_sz;
    int cfg_prm_val;
    uint16_t rec_cnt = 0;
    uint32_t fld_bitmsk;

    //Initialize parameters
    dev_cfg->dev_cfg_buf = NULL;
    dev_cfg->dev_cfg_cnt = 0;
    dev_cfg->dev_cfg_ver = 0;

    //Get file format version
    //Note: version must come before device setting records
    if (!hl_file_ver_get(file, &dev_cfg->dev_cfg_ver))
    {
        return ZW_ERR_VALUE;
    }

    //Check supported version
    if (dev_cfg->dev_cfg_ver != 1)
    {
        return ZW_ERR_VERSION;
    }

    //Calculate number of records
    file_pos = ftell(file);
    if (file_pos == -1)
    {
        return ZW_ERR_FILE;
    }

    while (fgets(line, 256, file))
    {
        if (*line == '#')
        {   //Skip comment line
            continue;
        }

        //Check if ',' exists
        if (strchr(line, ','))
        {
            rec_cnt++;
        }
    }

    //Allocate buffer for the records
    rec_buf = (dev_rec_t *) calloc(rec_cnt, sizeof(dev_rec_t));

    dev_cfg->dev_cfg_buf = rec_buf;

    if (!rec_buf)
    {
        return ZW_ERR_MEMORY;
    }

    //Get records
    if (fseek(file, file_pos, SEEK_SET) != 0)
    {
        return ZW_ERR_FILE;
    }

    rec_cnt = 0;

    while (fgets(line, 256, file))
    {
        if (*line == '#')
        {   //Skip comment line
            continue;
        }

        rec_str = line;
        fld_bitmsk = 0;

        //Get Manufacturer id
        field = hl_field_get(&rec_str);

#ifdef USE_SAFE_VERSION
        if (field && (sscanf_s(field, "%x", &vid) == 1))
#else
        if (field && (sscanf(field, "%x", &vid) == 1))
#endif
        {
            rec_buf->vid = (uint16_t)vid;
            fld_bitmsk = DEV_REC_MSK_VID;
        }

        //Get Product type
        field = hl_field_get(&rec_str);

#ifdef USE_SAFE_VERSION
        if (field && (sscanf_s(field, "%x", &ptype) == 1))
#else
        if (field && (sscanf(field, "%x", &ptype) == 1))
#endif
        {
            rec_buf->ptype = (uint16_t)ptype;
            fld_bitmsk |= DEV_REC_MSK_PTYPE;
        }

        //Get Product id
        field = hl_field_get(&rec_str);

#ifdef USE_SAFE_VERSION
        if (field && (sscanf_s(field, "%x", &pid) == 1))
#else
        if (field && (sscanf(field, "%x", &pid) == 1))
#endif
        {
            rec_buf->pid = (uint16_t)pid;
            fld_bitmsk |= DEV_REC_MSK_PID;
        }

        //Get device category
        field = hl_field_get(&rec_str);

#ifdef USE_SAFE_VERSION
        if (field && (sscanf_s(field, "%u", &category) == 1))
#else
        if (field && (sscanf(field, "%u", &category) == 1))
#endif
        {
            rec_buf->category = (uint8_t)category;
            fld_bitmsk |= DEV_REC_MSK_CAT;
        }

        //Get wakeup interval
        field = hl_field_get(&rec_str);

#ifdef USE_SAFE_VERSION
        if (field && (sscanf_s(field, "%u", &wkup_intv) == 1))
#else
        if (field && (sscanf(field, "%u", &wkup_intv) == 1))
#endif
        {
            rec_buf->wkup_intv = wkup_intv;
            fld_bitmsk |= DEV_REC_MSK_WKUP;
        }

        //Get group id
        field = hl_field_get(&rec_str);

#ifdef USE_SAFE_VERSION
        if (field && (sscanf_s(field, "%u", &grp_id) == 1))
#else
        if (field && (sscanf(field, "%u", &grp_id) == 1))
#endif
        {
            rec_buf->grp_id = (uint8_t)grp_id;
            fld_bitmsk |= DEV_REC_MSK_GID;
        }

        //Get configuration parameter number
        field = hl_field_get(&rec_str);

#ifdef USE_SAFE_VERSION
        if (field && (sscanf_s(field, "%u", &cfg_prm_num) == 1))
#else
        if (field && (sscanf(field, "%u", &cfg_prm_num) == 1))
#endif
        {
            rec_buf->cfg_prm_num = (uint8_t )cfg_prm_num;
            fld_bitmsk |= DEV_REC_MSK_CFG_NUM;
        }

        //Get configuration parameter size (in bytes)
        field = hl_field_get(&rec_str);

#ifdef USE_SAFE_VERSION
        if (field && (sscanf_s(field, "%u", &cfg_prm_sz) == 1))
#else
        if (field && (sscanf(field, "%u", &cfg_prm_sz) == 1))
#endif
        {
            rec_buf->cfg_prm_sz = (uint8_t)cfg_prm_sz;
            fld_bitmsk |= DEV_REC_MSK_CFG_SZ;
        }

        //Get configuration parameter value
        field = hl_field_get(&rec_str);

#ifdef USE_SAFE_VERSION
        if (field && (sscanf_s(field, "%d", &cfg_prm_val) == 1))
#else
        if (field && (sscanf(field, "%d", &cfg_prm_val) == 1))
#endif
        {
            rec_buf->cfg_prm_val = cfg_prm_val;
            fld_bitmsk |= DEV_REC_MSK_CFG_VAL;
        }

        //Save field bitmask
        if (fld_bitmsk)
        {
            rec_buf->fld_bitmsk = fld_bitmsk;
            //Adjustment
            rec_buf++;
            rec_cnt++;
        }
    }

    dev_cfg->dev_cfg_cnt = rec_cnt;

    if (rec_cnt == 0)
    {
        free(dev_cfg->dev_cfg_buf);
        dev_cfg->dev_cfg_buf = NULL;
    }

    return 0;
}

/**
hl_config_file_get - get device specific configuration file name and default parameter
@param[in]  file            Configuration file descriptor
@param[out] dev_file        Device specific configuration file path
@return     Number of parameters processed for the entry
*/
static int hl_config_file_get(FILE *file, char *dev_file)
{
    static const char delimiters[] = " =\r\n";
    char    line[384];
    char    *prm_name;
    char    *prm_val;
    int     param_cnt = 0;

    //Initialize parameters
    *dev_file = '\0';

    while ((param_cnt < 1) && (fgets(line, 384, file)))
    {
        if (*line == '#')
        {   //Skip comment line
            continue;
        }

        //Check if '=' exists
        if (strchr(line, '='))
        {
#ifdef USE_SAFE_VERSION
            char *next_token;
            //Get the parameter name and value
            prm_name = strtok_s(line, delimiters, &next_token);
#else
            //Get the parameter name and value
            prm_name = strtok(line, delimiters);
#endif

            if (prm_name)
            {
#ifdef USE_SAFE_VERSION
                prm_val = strtok_s(NULL, delimiters, &next_token);
#else
                prm_val = strtok(NULL, delimiters);
#endif

                if (!prm_val)
                {
                    continue;
                }

                //Compare the parameter name
                if (strcmp(prm_name, "DeviceCfgFile") == 0)
                {
#ifdef USE_SAFE_VERSION
                    strcpy_s(dev_file, 384, prm_val);
#else
                    strcpy(dev_file, prm_val);
#endif
                    param_cnt++;
                }
            }
        }
    }

    return param_cnt;
}

/**
hl_config_get - Get configuration parameters
@param[out]	    dev_cfg  Device specific settings
@return  Zero on success, non-zero on failure.
*/
static int hl_config_get(dev_spec_cfg_t *dev_cfg, const char* cfgfile, const char* filepath)
{
    FILE            *file;
    int             ret;
    char            dev_file[384] = {0};
    char            dev_file_path[1024] = {0};

#ifdef USE_SAFE_VERSION
    if (fopen_s(&file, cfgfile, "rt") != 0)
#else
    file = fopen(cfgfile, "rt");
    if (!file)
#endif
    {
        ALOGE("hl_config_get can't open file:%s\n", cfgfile);
        return ZW_ERR_FILE_OPEN;
    }

    //Get device specific configuration file paths
    ret = hl_config_file_get(file, dev_file);

    fclose(file);

    if (ret != 1)
    {
        ALOGE("Failed to get file path and configuration parameter!\n");
        return ZW_ERR_VALUE;
    }

    strcpy(dev_file_path, filepath);
    strcat(dev_file_path, dev_file);

    //Get device specific settings
#ifdef USE_SAFE_VERSION
    if (fopen_s(&file, dev_file, "rt") != 0)
#else
    file = fopen(dev_file_path, "rt");
    if (!file)
#endif
    {
        ALOGE("Failed to open device settings file.\n");
        return ZW_ERR_FILE_OPEN;
    }

    ret = hl_dev_setting_get(file, dev_cfg);
    fclose(file);

    if (ret != 0)
    {
        ALOGE("hl_dev_setting_get with error:%d\n", ret);
        return ret;
    }

    ALOGD("Number of device settings records:%u\n", dev_cfg->dev_cfg_cnt);

    return 0;
}

/**
hl_plt_ctx_get - Get platform context
@return         platform context
*/
static void  *hl_plt_ctx_get(hl_appl_ctx_t *hl_appl)
{
    if (!hl_appl->plt_ctx)
    {
        zwnetd_t    *net_desc;
        //Get and save the context
        net_desc = zwnet_get_desc(hl_appl->zwnet);

        hl_appl->plt_ctx = net_desc->plt_ctx;
    }

    return hl_appl->plt_ctx;
}

/**
hl_desc_id_get - get node descriptor id
@param[in]	head	    The head of the descriptor container linked-list
@param[in]	noded	Node descriptor
@return     Node descriptor id if found; else return 0
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
static uint32_t  hl_desc_id_get(desc_cont_t *head, zwnoded_p noded)
{
    desc_cont_t     *curr_node_cont;
    zwnoded_p       curr_node_desc;     //current node descriptor

    //Start searching from the first node
    curr_node_cont = head;

    while (curr_node_cont)
    {
        curr_node_desc = (zwnoded_p)curr_node_cont->desc;

        if (curr_node_desc->nodeid == noded->nodeid)
        {
            return curr_node_cont->id;
        }

        //Get the next node
        curr_node_cont = curr_node_cont->next;
    }

    return 0;
}

/**
hl_desc_cont_del - delete node descriptor container for the specified id
@param[in]	head	    The head of the descriptor container linked-list
@param[in]	desc_id		Descriptor id of the node descriptor container
@return
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
static void hl_desc_cont_del(desc_cont_t **head, uint32_t desc_id)
{
    desc_cont_t     *last_node_cont;
    desc_cont_t     *last_ep_cont;
    desc_cont_t     *last_intf_cont;
    desc_cont_t     *next_desc_cont;
    desc_cont_t     *prev_desc_cont;    //Previous descriptor container
    zwifd_p         ifd;

    //Start searching from the first node
    last_node_cont = *head;
    prev_desc_cont = NULL;

    while (last_node_cont)
    {
        if (last_node_cont->id == desc_id)
        {
            //Delete endpoint
            last_ep_cont = last_node_cont->down;

            while (last_ep_cont)
            {

                //Delete interface
                last_intf_cont = last_ep_cont->down;

                while (last_intf_cont)
                {
                    //Store the next interface container
                    next_desc_cont = last_intf_cont->next;

                    //Free command class specific data
                    ifd = (zwifd_p)last_intf_cont->desc;

                    if (ifd->data_cnt > 0)
                    {
                        free(ifd->data);
                    }

                    //Free interface container
                    free(last_intf_cont);

                    //Get the next interface
                    last_intf_cont = next_desc_cont;
                }

                //Store the next endpoint container
                next_desc_cont = last_ep_cont->next;

                free(last_ep_cont);

                //Get the next endpoint
                last_ep_cont = next_desc_cont;
            }
            //Check whether this is the first node
            if (!prev_desc_cont)
            {   //First node
                *head = last_node_cont->next;
                free(last_node_cont);
                return;
            }
            //Not the first node
            prev_desc_cont->next = last_node_cont->next;
            free(last_node_cont);
            return;

        }

        //Get the next node
        prev_desc_cont = last_node_cont;
        last_node_cont = last_node_cont->next;
    }
}

/**
hl_desc_id_gen - genarate unique descriptor id
@param[in]	nw		Network
@return         Generated descriptor id
*/
static uint32_t  hl_desc_id_gen(zwnet_p nw)
{
    hl_appl_ctx_t       *hl_appl;
    zwnetd_t            *net_desc;

    net_desc = zwnet_get_desc(nw);

    hl_appl = (hl_appl_ctx_t *)net_desc->user;

    if (hl_appl->desc_id == 0)
    {   //ID of zero is invalid
        hl_appl->desc_id++;
    }
    return hl_appl->desc_id++;
}

/**
hl_desc_cont_add - add a node into the descriptor container linked-list
@param[in]	head	The head of the descriptor container linked-list
@param[in]	noded	Node descriptor
@return Zero on success; else return ZW_ERR_XXX
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
static int  hl_desc_cont_add(desc_cont_t **head, zwnoded_p noded)
{
    uint32_t    desc_id;
    int         result;
    zwepd_t     ep_desc;
    zwifd_t     ifd;
    zwnoded_t   updt_node;
    zwnoded_p   node;
    zwepd_p     ep;
    zwifd_p     intf;
    desc_cont_t     *last_node_cont;
    desc_cont_t     *last_ep_cont;
    desc_cont_t     *last_intf_cont;
    desc_cont_t     *new_node_cont;

    //Check whether the node already exists
    desc_id = hl_desc_id_get(*head, noded);

    if (desc_id)
    {   //Delete the existing node container
        hl_desc_cont_del(head, desc_id);
    }

    //Get the updated node descriptor
    if (zwnode_get_ep(noded, &ep_desc) < 0)
    {
        return ZW_ERR_EP_NOT_FOUND;
    }

    if (zwep_get_node(&ep_desc, &updt_node) < 0)
    {
        return ZW_ERR_NODE_NOT_FOUND;
    }

    //Check whether this is the first node to be added
    if (*head == NULL)
    {   //This is the first node to be added
        new_node_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t)
                                                 + sizeof(zwnoded_t) - 1);
        if (!new_node_cont)
        {
            return ZW_ERR_MEMORY;
        }
        *head = new_node_cont;

    }
    else
    {   //Add new node container at the end of the list
        last_node_cont = *head;

        while (last_node_cont->next)
        {
            //Get the next node
            last_node_cont = last_node_cont->next;
        }
        new_node_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t)
                                                 + sizeof(zwnoded_t) - 1);
        if (!new_node_cont)
        {
            return ZW_ERR_MEMORY;
        }

        last_node_cont->next = new_node_cont;
    }
    //Init the node container
    new_node_cont->type = DESC_TYPE_NODE;
    new_node_cont->id = hl_desc_id_gen(noded->net);
    node = (zwnoded_p)new_node_cont->desc;
    *node = updt_node;

    //Add endpoints
    last_ep_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwepd_t) - 1);
    if (!last_ep_cont)
    {
        return ZW_ERR_MEMORY;
    }
    last_ep_cont->type = DESC_TYPE_EP;
    last_ep_cont->id = hl_desc_id_gen(noded->net);
    ep = (zwepd_p)last_ep_cont->desc;
    zwnode_get_ep(node, ep);
    new_node_cont->down = last_ep_cont;

    while (ep)
    {
        if (zwep_get_if(ep, &ifd) < 0)
        {
            break;
        }

        //Add interfaces
        last_intf_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwifd_t) - 1);
        if (!last_intf_cont)
        {
            return ZW_ERR_MEMORY;
        }

        last_intf_cont->type = DESC_TYPE_INTF;
        last_intf_cont->id = hl_desc_id_gen(noded->net);
        intf = (zwifd_p)last_intf_cont->desc;
        *intf = ifd;
        last_ep_cont->down = last_intf_cont;

        while (intf)
        {
            //Get the next interface
            result = zwif_get_next(intf, &ifd);
            if (result == 0)
            {
                desc_cont_t     *intf_cont;
                zwifd_p         ifdp;

                intf = &ifd;
                intf_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwifd_t) - 1);
                if (!intf_cont)
                {
                    return ZW_ERR_MEMORY;
                }
                intf_cont->type = DESC_TYPE_INTF;
                intf_cont->id = hl_desc_id_gen(noded->net);
                ifdp = (zwifd_p)intf_cont->desc;
                *ifdp = ifd;
                last_intf_cont->next = intf_cont;
                last_intf_cont = intf_cont;
            }
            else
            {
                intf = NULL;
            }
        }

        //Get the next endpoint
        result = zwep_get_next(ep, &ep_desc);
        if (result == 0)
        {
            desc_cont_t     *ep_cont;
            zwepd_p         epp;

            ep = &ep_desc;
            ep_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwepd_t) - 1);
            if (!ep_cont)
            {
                return ZW_ERR_MEMORY;
            }
            ep_cont->type = DESC_TYPE_EP;
            ep_cont->id = hl_desc_id_gen(noded->net);
            epp = (zwepd_p)ep_cont->desc;
            *epp = ep_desc;
            last_ep_cont->next = ep_cont;
            last_ep_cont = ep_cont;
        }
        else
        {
            ep = NULL;
        }
    }

    return 0;
}

/**
hl_nw_node_cb - Callback function to notify node is added, deleted, or updated
@param[in]	user	    The high-level api context
@param[in]	noded	Node
@param[in]	mode	    The node status
@return
*/
static void hl_nw_node_cb(void *user, zwnoded_p noded, int mode)
{
    hl_appl_ctx_t   *hl_appl = (hl_appl_ctx_t *)user;

    switch (mode)
    {
        case ZWNET_NODE_ADDED:
            {
                plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_nw_node_cb node:%u added", (unsigned)noded->nodeid);
                //Store the last added node descriptor
                hl_appl->node_add_desc = *noded;

                //Add node descriptor container
                plt_mtx_lck(hl_appl->desc_cont_mtx);
                //hl_desc_cont_add(&hl_appl->desc_cont_hd, noded); //djnakata
                plt_mtx_ulck(hl_appl->desc_cont_mtx);
            }
            break;

        case ZWNET_NODE_REMOVED:
            {
                plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_nw_node_cb node:%u removed", (unsigned)noded->nodeid);
                //Remove the node descriptor container
                plt_mtx_lck(hl_appl->desc_cont_mtx);
                hl_desc_cont_del(&hl_appl->desc_cont_hd, hl_desc_id_get(hl_appl->desc_cont_hd, noded));
                plt_mtx_ulck(hl_appl->desc_cont_mtx);
            }
            break;

        case ZWNET_NODE_UPDATED:
            {
                plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_nw_node_cb node:%u updated", (unsigned)noded->nodeid);
                //Store the last replaced node descriptor
                hl_appl->node_rp_desc = *noded;

                //Update the node descriptor container
                plt_mtx_lck(hl_appl->desc_cont_mtx);
                //hl_desc_cont_updt(&hl_appl->desc_cont_hd, noded);
                hl_desc_cont_add(&hl_appl->desc_cont_hd, noded);
                plt_mtx_ulck(hl_appl->desc_cont_mtx);
            }
            break;

        default:
            break;
    }
}

/**
hl_desc_cont_rm_all - remove the whole descriptor container linked-list
@param[in]	head	    The head of the descriptor container linked-list
@return
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
static void hl_desc_cont_rm_all(desc_cont_t **head)
{
    while (*head)
    {
        hl_desc_cont_del(head, (*head)->id);
    }
}

/**
hl_desc_init - Initialize the descriptor linked-list
@param[in]	head	The head of the descriptor container linked-list
@param[in]	nw		Network
@return Zero on success; else return ZW_ERR_XXX
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
static int hl_desc_init(desc_cont_t **head, zwnet_p nw)
{
    int         result;
    zwnoded_t   noded;
    zwepd_t     ep_desc;
    zwifd_t     ifd;
    zwnoded_p   node;
    zwepd_p     ep;
    zwifd_p     intf;
    desc_cont_t *last_node_cont;
    desc_cont_t *last_ep_cont;
    desc_cont_t *last_intf_cont;

    *head = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwnoded_t) - 1);
    if (*head == NULL)
    {
        return ZW_ERR_MEMORY;
    }
    (*head)->type = DESC_TYPE_NODE;
    (*head)->id = hl_desc_id_gen(nw);
    node = (zwnoded_p)(*head)->desc;
    last_node_cont = *head;

    result = zwnet_get_node(nw, node);
    if (result != 0)
    {
        //plt_msg_show("hl_desc_init get controller node with error:%d", result);
        return result;
    }

    while (node)
    {
        last_ep_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwepd_t) - 1);
        if (!last_ep_cont)
        {
            return ZW_ERR_MEMORY;
        }
        last_ep_cont->type = DESC_TYPE_EP;
        last_ep_cont->id = hl_desc_id_gen(nw);
        ep = (zwepd_p)last_ep_cont->desc;
        zwnode_get_ep(node, ep);
        last_node_cont->down = last_ep_cont;

        while (ep)
        {
            if (zwep_get_if(ep, &ifd) < 0)
            {
                break;
            }

            //Add interfaces
            last_intf_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwifd_t) - 1);
            if (!last_intf_cont)
            {
                return ZW_ERR_MEMORY;
            }

            last_intf_cont->type = DESC_TYPE_INTF;
            last_intf_cont->id = hl_desc_id_gen(nw);
            intf = (zwifd_p)last_intf_cont->desc;
            *intf = ifd;
            last_ep_cont->down = last_intf_cont;

            while (intf)
            {
                //Get the next interface
                result = zwif_get_next(intf, &ifd);
                if (result == 0)
                {
                    desc_cont_t     *intf_cont;
                    zwifd_p         ifdp;

                    intf = &ifd;
                    intf_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwifd_t) - 1);
                    if (!intf_cont)
                    {
                        return ZW_ERR_MEMORY;
                    }
                    intf_cont->type = DESC_TYPE_INTF;
                    intf_cont->id = hl_desc_id_gen(nw);
                    ifdp = (zwifd_p)intf_cont->desc;
                    *ifdp = ifd;
                    last_intf_cont->next = intf_cont;
                    last_intf_cont = intf_cont;
                }
                else
                {
                    intf = NULL;
                }
            }

            //Get the next endpoint
            result = zwep_get_next(ep, &ep_desc);
            if (result == 0)
            {
                desc_cont_t     *ep_cont;
                zwepd_p         epp;

                ep = &ep_desc;
                ep_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwepd_t) - 1);
                if (!ep_cont)
                {
                    return ZW_ERR_MEMORY;
                }
                ep_cont->type = DESC_TYPE_EP;
                ep_cont->id = hl_desc_id_gen(nw);
                epp = (zwepd_p)ep_cont->desc;
                *epp = ep_desc;
                last_ep_cont->next = ep_cont;
                last_ep_cont = ep_cont;

            }
            else
            {
                ep = NULL;
            }
        }

        //Get the next node
        result = zwnode_get_next(node, &noded);
        if (result == 0)
        {
            desc_cont_t     *node_cont;
            zwnoded_p       nodedp;

            node = &noded;

            node_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwnoded_t) - 1);
            if (!node_cont)
            {
                return ZW_ERR_MEMORY;
            }
            node_cont->type = DESC_TYPE_NODE;
            node_cont->id = hl_desc_id_gen(nw);
            nodedp = (zwnoded_p)node_cont->desc;
            *nodedp = noded;
            last_node_cont->next = node_cont;
            last_node_cont = node_cont;
        }
        else
        {
            node = NULL;
        }
    }
    return 0;
}

static int hl_destid_get(hl_appl_ctx_t *hl_appl, int nodeId, int cmd)
{
    int result, find = 0;
    zwnetd_p    net_desc;
    zwnoded_p   node;
    zwepd_p     ep;
    zwifd_p     intf;
    desc_cont_t *last_node_cont;
    desc_cont_t *last_ep_cont;
    desc_cont_t *last_intf_cont;

    plt_mtx_lck(hl_appl->desc_cont_mtx);

    //Check whether the descriptor container linked list is initialized
    if (!hl_appl->desc_cont_hd)
    {
        result = hl_desc_init(&hl_appl->desc_cont_hd, hl_appl->zwnet);
        if (result != 0)
        {
            ALOGD("hl_destid_get with error:%d", result);
            plt_mtx_ulck(hl_appl->desc_cont_mtx);
            return -1;
        }
    }

    //Get the first node (local controller) and home id
    last_node_cont = hl_appl->desc_cont_hd;

    net_desc = zwnet_get_desc(hl_appl->zwnet);

    while (last_node_cont)
    {
        if (last_node_cont->type != DESC_TYPE_NODE)
        {
            ALOGD("node: wrong desc type:%u", last_node_cont->type);
        }

        node = (zwnoded_p)last_node_cont->desc;

        if((unsigned)node->nodeid == nodeId)
        {
            ALOGD("Get Basic node found, id %d",nodeId);
            break;
        }
        else{
            //Get the next node
            last_node_cont = last_node_cont->next;
        }
    }

    if(last_node_cont == NULL)
    {
        ALOGE("The request node isn't found, please try another");
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return -1;
    }

    last_ep_cont = last_node_cont->down;

    while (last_ep_cont)
    {
        if (last_ep_cont->type != DESC_TYPE_EP)
        {
            ALOGD("ep: wrong desc type:%u", last_ep_cont->type);
        }

        ep = (zwepd_p)last_ep_cont->desc;

        //Get interface
        last_intf_cont = last_ep_cont->down;

        while (last_intf_cont)
        {
            if (last_intf_cont->type != DESC_TYPE_INTF)
            {
                ALOGD("interface: wrong desc type:%u", last_intf_cont->type);
            }

            intf = (zwifd_p)last_intf_cont->desc;

/*            ALOGD("              Interface: %02Xv%u:%s [%u]%c%c",
                  (unsigned)intf->cls, intf->ver, hl_class_str_get(intf->cls, intf->ver),
                  last_intf_cont->id, (intf->propty & IF_PROPTY_SECURE)? '*' : ' ',
                  (intf->propty & IF_PROPTY_UNSECURE)? '^' : ' ');*/

            if((unsigned)intf->cls == cmd)
            {
                ALOGD("required interface found");
                hl_appl->dst_desc_id = last_intf_cont->id;
                hl_appl->rep_desc_id = last_intf_cont->id;
                hl_appl->temp_desc = last_intf_cont->id;
                find = 1;
                break;
            }

            //Get the next interface
            last_intf_cont = last_intf_cont->next;
        }

        //Get the next endpoint
        last_ep_cont = last_ep_cont->next;
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if(find == 1)
    {
        return 0;
    }

    return -1;
}

/**
hl_node_desc_get - get node descriptor from descriptor container
@param[in]	head	    The head of the descriptor container linked-list
@param[in]	desc_id		Unique descriptor id
@return     Node descriptor if found; else return NULL
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
static zwnoded_p  hl_node_desc_get(desc_cont_t *head, uint32_t desc_id)
{
    desc_cont_t     *last_node_cont;

    //Start searching from the first node
    last_node_cont = head;

    while (last_node_cont)
    {
        if (last_node_cont->id == desc_id)
        {
            if (last_node_cont->type == DESC_TYPE_NODE)
            {
                return(zwnoded_p)last_node_cont->desc;
            }
            //plt_msg_ts_show("hl_node_desc_get desc id:%u is not type node", desc_id);
            return NULL;
        }

        //Get the next node
        last_node_cont = last_node_cont->next;
    }
    //plt_msg_ts_show("hl_node_desc_get invalid desc id:%u", desc_id);
    return NULL;
}

/**
hl_node_updt - Update a node information
@param[in]	hl_appl		The high-level api context
@return  0 on success, negative error number on failure
*/
static int32_t hl_node_updt(hl_appl_ctx_t *hl_appl)
{
    int32_t     result;
    zwnoded_p   noded;

    //Get the node descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    noded = hl_node_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);

    if (!noded)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_NODE_NOT_FOUND;
    }

    hl_appl->node_updt_desc = *noded;
    ALOGD("node update, id= %d",noded->nodeid);
    result = zwnode_update(noded);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_node_updt with error:%d", result);
    }
    return result;
}

static void hl_nw_notify_hdlr(nw_notify_msg_t *notify_msg)
{
    hl_appl_ctx_t    *hl_appl = notify_msg->hl_appl;
    int              result;

    //Check to display progress of get detailed node info
    if (notify_msg->sts & OP_GET_NI_TOTAL_NODE_MASK)
    {
        uint16_t    total_nodes;
        uint16_t    cmplt_nodes;

        total_nodes = (uint16_t)((notify_msg->sts & OP_GET_NI_TOTAL_NODE_MASK) >> 8);
        cmplt_nodes = (uint16_t)(notify_msg->sts & OP_GET_NI_NODE_CMPLT_MASK);
        plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_nw_notify_cb op:%u, get node info %u/%u completed",
                        (unsigned)notify_msg->op, cmplt_nodes, total_nodes);
        return;
    }

    plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_nw_notify_cb op:%u, status:%u", (unsigned)notify_msg->op, notify_msg->sts);

    switch (notify_msg->op)
    {
        case ZWNET_OP_UPDATE://TODO: update all the nodes, instead of rebuilding.
        case ZWNET_OP_INITIALIZE:
        case ZWNET_OP_INITIATE:
        case ZWNET_OP_RESET:

            if (notify_msg->sts == ZW_ERR_NONE)
            {   //Rebuild the descriptor container linked-list
                plt_mtx_lck(hl_appl->desc_cont_mtx);
                hl_desc_cont_rm_all(&hl_appl->desc_cont_hd);
                result = hl_desc_init(&hl_appl->desc_cont_hd, hl_appl->zwnet);
                if (result != 0)
                {
                    plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_nw_notify_hdlr with error:%d", result);
                }

                plt_mtx_ulck(hl_appl->desc_cont_mtx);
            }
            break;

        case ZWNET_OP_NODE_UPDATE:
            {
                //Check whether stress test in progress
                if (hl_appl->is_ni_stress_tst_run)
                {
                    hl_node_updt(hl_appl);
                    break;
                }

                //Update the node descriptor container
                plt_mtx_lck(hl_appl->desc_cont_mtx);
                //hl_desc_cont_updt(&hl_appl->desc_cont_hd, &hl_appl->node_updt_desc);
                hl_desc_cont_add(&hl_appl->desc_cont_hd, &hl_appl->node_updt_desc);
                plt_mtx_ulck(hl_appl->desc_cont_mtx);
            }
            break;

        case ZWNET_OP_ADD_NODE:
        case ZWNET_OP_MIGRATE:
        case ZWNET_OP_MIGRATE_SUC:
            if (notify_msg->sts == ZW_ERR_NONE)
            {
                //Update the node descriptor container
                plt_mtx_lck(hl_appl->desc_cont_mtx);
                hl_desc_cont_add(&hl_appl->desc_cont_hd, &hl_appl->node_add_desc);
                plt_mtx_ulck(hl_appl->desc_cont_mtx);
            }
            break;

        case ZWNET_OP_RP_NODE:
            if (notify_msg->sts == ZW_ERR_NONE)
            {
                //Update the node descriptor container
                plt_mtx_lck(hl_appl->desc_cont_mtx);
                hl_desc_cont_add(&hl_appl->desc_cont_hd, &hl_appl->node_rp_desc);
                plt_mtx_ulck(hl_appl->desc_cont_mtx);
            }
            break;
    }
}

static void dummy_post_msg(void *msg)
{
    hl_nw_notify_hdlr((nw_notify_msg_t *)msg);
    free(msg);
}

static char* hl_nw_create_op_msg(uint8_t op, uint16_t sts)
{
    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return NULL;
    }

    if(op == ZWNET_OP_ADD_NODE)
    {
        cJSON_AddStringToObject(jsonRoot, "MessageType", "Node Add Status");

        if(sts == OP_FAILED)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Failed");
        }
        else if(sts == OP_ADD_NODE_ADDING)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Adding");
        }
        else if(sts == OP_ADD_NODE_PROTOCOL_DONE)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Protocol Done");
        }
        else if(sts == OP_ADD_NODE_LEARN_READY)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Learn Ready");
        }
        else if(sts == OP_ADD_NODE_SEC_INCD)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Adding Node Securely");
        }
        else if(sts == OP_ADD_NODE_GET_NODE_INFO)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Getting Node Information");
        }
        else if(sts == OP_ADD_NODE_FOUND)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Node Found");
        }
        else if(sts == OP_DONE)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Success");
        }

        char *p = cJSON_Print(jsonRoot);

        if(p == NULL)
        {
            cJSON_Delete(jsonRoot);
            return NULL;
        }

        cJSON_Delete(jsonRoot);

        return p;
    }
    else if(op == ZWNET_OP_RM_NODE)
    {
        cJSON_AddStringToObject(jsonRoot, "MessageType", "Node Remove Status");

        if(sts == OP_FAILED)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Failed");
        }
        else if(sts == OP_RM_NODE_REMOVING)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Removing");
        }
        else if(sts == OP_RM_NODE_LEARN_READY)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Learn Ready");
        }
        else if(sts == OP_RM_NODE_FOUND)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Node Found");
        }
        else if(sts == OP_DONE)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Success");
        }

        char *p = cJSON_Print(jsonRoot);

        if(p == NULL)
        {
            cJSON_Delete(jsonRoot);
            return NULL;
        }

        cJSON_Delete(jsonRoot);

        return p;
    }

    return NULL;
}

/**
hl_nw_notify_cb - Callback function to notify the status of current operation
@param[in]	user	The high-level api context
param[in]	op		Network operation ZWNET_OP_XXX
@param[in]	ret		The status of current operation
@return
*/
static void hl_nw_notify_cb(void *user, uint8_t op, uint16_t sts)
{
    nw_notify_msg_t  *nw_notify;

    nw_notify = (nw_notify_msg_t *)malloc(sizeof(nw_notify_msg_t));

    if (nw_notify)
    {
        nw_notify->hl_appl = (hl_appl_ctx_t *)user;
        nw_notify->op = op;
        nw_notify->sts = sts;

        //Post message to main windows
        //PostMessage(ghWnd, MSG_ZWAPI_NOTIFY, 0, (LPARAM )nw_notify);
        dummy_post_msg(nw_notify);

        char *str = hl_nw_create_op_msg(op, sts);

        if(str != NULL)
        {
            if(resCallBack)
            {
                resCallBack(str);
            }
            
            free(str);
        }
    }
}

/**
hl_nw_tx_cb - Callback function to notify application transmit data status
@param[in]	user	    The high-level api context
param[in]	tx_sts	    Transmit status ZWNET_TX_xx
@return
*/
static void hl_nw_tx_cb(void *user, uint8_t tx_sts)
{
    hl_appl_ctx_t   *hl_appl = (hl_appl_ctx_t *)user;

    static char    *tx_cmplt_sts[] = {"ok",
                                      "no ACK before timeout",
                                      "failed",
                                      "routing not idle",
                                      "no route",
                                      "no callback frame before timeout"
    };

    if (tx_sts == TRANSMIT_COMPLETE_OK)
    {
        //plt_msg_show(hl_plt_ctx_get(hl_appl), "Higher level appl send data completed successfully");
    }
    else
    {
        plt_msg_show(hl_plt_ctx_get(hl_appl), "Higher level appl send data completed with error:%s",
                     (tx_sts < sizeof(tx_cmplt_sts)/sizeof(char *))?
                     tx_cmplt_sts[tx_sts]  : "unknown");
    }
}

/**
hl_msg_show - show message to the user
@param[in] msg   The output message to printf.
@return
*/
static void hl_msg_show(void *msg)
{
    ALOGD("%s", (const char *)msg);
}

/**
hl_close - Close connection to controller
@param[in]	hl_appl		The high-level api context
@return
*/
static void hl_close(hl_appl_ctx_t *hl_appl)
{
#ifdef  USER_APPL_DEVICE_CFG
    int i;
#endif

    zwnet_exit(hl_appl->zwnet,
               (hl_appl->save_ni_file)? hl_appl->node_info_file : NULL);

    //Remove all descriptor container entries
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    hl_desc_cont_rm_all(&hl_appl->desc_cont_hd);
    plt_mtx_ulck(hl_appl->desc_cont_mtx);
    plt_mtx_destroy(hl_appl->desc_cont_mtx);

    //Update init status
    hl_appl->is_init_done = 0;

#ifdef  USER_APPL_DEVICE_CFG
    for (i=0; i<4; i++)
        free(hl_appl->dev_cfg_bufs[i].dev_cfg_buf);
#endif
}

static int hl_init(hl_appl_ctx_t *hl_appl, const char *cfgfile, const char* filepath, cJSON *jsonRoot)
{
    int                 result;
    zwnet_init_t        zw_init = {0};
    zwnet_init_ret_t    zw_init_ret;
    dev_spec_cfg_t      dev_spec_cfg;
#ifdef  USER_APPL_DEVICE_CFG
    dev_spec_cfg_usr_t  dev_spec_cfg_usr;
    int                 i;
#endif
    char                net_id[10];

    //Get configuration parameters (wake up and sensor settings) from file
    result = hl_config_get(&dev_spec_cfg, cfgfile, filepath);

    if (result != 0)
    {
        ALOGE("hl_config_get with error:%d\n", result);
        return result;
    }

#ifdef  USER_APPL_DEVICE_CFG
    //Load and save device specific configurations
    if (dev_spec_cfg.dev_cfg_cnt && dev_spec_cfg.dev_cfg_buf)
    {
        result = hl_dev_cfg_load(&dev_spec_cfg, hl_appl->dev_cfg_bufs);
        if (result < 0)
        {
            ALOGE("hl_dev_cfg_load with error:%d\n", result);
            free(dev_spec_cfg.dev_cfg_buf);
            return result;
        }
    }

    dev_spec_cfg_usr.dev_cfg_ctx = hl_appl;
    dev_spec_cfg_usr.dev_cfg_ver = dev_spec_cfg.dev_cfg_ver;
    dev_spec_cfg_usr.dev_rec_find_fn = hl_dev_rec_find;
#endif

    //Init high-level appl layer
    if (!plt_mtx_init(&hl_appl->desc_cont_mtx))
    {
        result = ZW_ERR_NO_RES;
        goto l_HL_INIT_ERROR1;
    }

    zw_init.instance = 0;
    zw_init.user = hl_appl; //high-level application context
    zw_init.node = hl_nw_node_cb;
    zw_init.notify = hl_nw_notify_cb;
    zw_init.appl_tx = hl_nw_tx_cb;
#ifdef  SUPPORT_SECURITY
    zw_init.sec_enable = 1;
#else
    zw_init.sec_enable = 0;
#endif
    zw_init.print_txt_fn = hl_msg_show;
#ifdef  USER_APPL_DEVICE_CFG
    zw_init.dev_spec_cfg_usr = &dev_spec_cfg_usr;
#else
    zw_init.dev_spec_cfg = &dev_spec_cfg;
#endif

    //Init ZW network
    result = zwnet_init(&zw_init, &zw_init_ret, jsonRoot);

    if (result != 0)
    {
        ALOGE("hl_init with error:%d\n", result);

        if (result == ZW_ERR_NO_RESP)
        {
            ALOGE("The controller's NVM might be corrupted, please select Device->Quick reset of controller.\n");
        }

        goto l_HL_INIT_ERROR2;
    }

    hl_appl->zwnet = zw_init_ret.net;

    sprintf(net_id, "%08X", zw_init_ret.net_id);

    plt_msg_show(hl_plt_ctx_get(hl_appl), "network id:%s, controller id:%u, HC API type:%u",
                 net_id, zw_init_ret.ctlr_id, zw_init_ret.hc_api_type);

    result = zwnet_load(hl_appl->zwnet,
                        (hl_appl->load_ni_file)? hl_appl->node_info_file : NULL);

    if (result != 0)
    {
        plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_init load node info with error:%d", result);
        goto l_HL_INIT_ERROR3;
    } else if(result == 0){
        ALOGI("Init: load node info successfully");
    }

    //Rebuild the descriptor container linked-list
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    hl_desc_cont_rm_all(&hl_appl->desc_cont_hd);
    result = hl_desc_init(&hl_appl->desc_cont_hd, hl_appl->zwnet);
    if (result != 0)
    {
        plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_desc_init with error:%d", result);
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        goto l_HL_INIT_ERROR3;
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    //Update init status
    hl_appl->is_init_done = 1;

#ifdef  ZW_STRESS_TEST
    zwnet_stress_tst_cb_set(hl_appl->zwnet, hl_stress_tst_cb);
#endif

    //Free device specific setting buffers
    free(dev_spec_cfg.dev_cfg_buf);
    return 0;

    l_HL_INIT_ERROR3:
        zwnet_exit(hl_appl->zwnet, NULL);
    l_HL_INIT_ERROR2:
        plt_mtx_destroy(hl_appl->desc_cont_mtx);
    l_HL_INIT_ERROR1:
        free(dev_spec_cfg.dev_cfg_buf);
#ifdef  USER_APPL_DEVICE_CFG
    for (i=0; i<4; i++)
        free(hl_appl->dev_cfg_bufs[i].dev_cfg_buf);
#endif
    return result;
}

/**
hl_if_plt_ctx_get - Get platform context
@return         platform context
*/
static void* hl_if_plt_ctx_get(zwifd_p ifd)
{
    zwnetd_t    *net_desc;
    //Get and save the context
    net_desc = zwnet_get_desc(ifd->net);
    return net_desc->plt_ctx;
}

static void hl_grp_info_show(zwifd_p intf, cJSON *interfaceInfo)
{
    int                 j;
    int                 i;
    int                 result;
    if_grp_info_dat_t   *grp_info;
    void                *plt_ctx;
    zw_grp_info_p       grp_info_ent;
    char str[50] = {0};

    result = zwif_group_info_get(intf, &grp_info);

    plt_ctx = hl_if_plt_ctx_get(intf);

    if (result == 0)
    {
        plt_msg_show(plt_ctx, "                        Group info type:%s", (grp_info->dynamic)? "dynamic" : "static");
        plt_msg_show(plt_ctx, "                        Maximum supported groups:%u", grp_info->group_cnt);
        plt_msg_show(plt_ctx, "                        Valid groups:%u", grp_info->valid_grp_cnt);

        cJSON_AddStringToObject(interfaceInfo, "Group info type", (grp_info->dynamic)? "dynamic" : "static");
        cJSON_AddNumberToObject(interfaceInfo, "Maximum supported groups", grp_info->group_cnt);
        cJSON_AddNumberToObject(interfaceInfo, "Valid groups", grp_info->valid_grp_cnt);

        cJSON * grpInfoArray =  cJSON_CreateArray();

        if(grpInfoArray == NULL)
        {
            return;
        }

        cJSON_AddItemToObject(interfaceInfo, "Group Info List", grpInfoArray);

        for (i=0; i<grp_info->valid_grp_cnt; i++)
        {
            grp_info_ent = grp_info->grp_info[i];

            if (grp_info_ent)
            {
                cJSON *grpInfo = cJSON_CreateObject();

                if(grpInfo == NULL)
                {
                    return;
                }

                cJSON_AddItemToArray(grpInfoArray, grpInfo);

                plt_msg_show(plt_ctx, "                        --------------------------------------------");
                plt_msg_show(plt_ctx, "                        Group id:%u, profile:%04xh, event code:%04xh,",
                             grp_info_ent->grp_num, grp_info_ent->profile, grp_info_ent->evt_code);
                plt_msg_show(plt_ctx, "                        name:%s, command list:",
                             grp_info_ent->name);

                cJSON_AddNumberToObject(grpInfo, "Group id", grp_info_ent->grp_num);

                sprintf(str, "%04xh", grp_info_ent->profile);
                cJSON_AddStringToObject(grpInfo, "profile", str);

                sprintf(str, "%04xh", grp_info_ent->evt_code);
                cJSON_AddStringToObject(grpInfo, "event code", str);

                cJSON * cmdInfoArray =  cJSON_CreateArray();

                if(cmdInfoArray == NULL)
                {
                    return;
                }

                cJSON_AddItemToObject(grpInfo, "Cmd Info List", cmdInfoArray);

                for (j=0; j<grp_info_ent->cmd_ent_cnt; j++)
                {
                    cJSON *cmdInfo = cJSON_CreateObject();

                    if(cmdInfo == NULL)
                    {
                        return;
                    }

                    cJSON_AddItemToArray(cmdInfoArray, cmdInfo);

                    plt_msg_show(plt_ctx, "                        command class:%04xh(%s), command:%02xh",
                                 grp_info_ent->cmd_lst[j].cls,
                                 hl_class_str_get(grp_info_ent->cmd_lst[j].cls, 1),
                                 grp_info_ent->cmd_lst[j].cmd);

                    cJSON_AddStringToObject(cmdInfo, "command class", hl_class_str_get(grp_info_ent->cmd_lst[j].cls, 1));
                    sprintf(str, "%02xh", grp_info_ent->cmd_lst[j].cmd);
                    cJSON_AddStringToObject(cmdInfo, "command", str);
                }
            }
        }

        //Free group info
        zwif_group_info_free(grp_info);
    }
}

/**
hl_ext_ver_show - Show extended version information
@param[in]	hl_appl   The high-level api context
@param[in]	node	  Node
@return
*/
static void hl_ext_ver_show(hl_appl_ctx_t *hl_appl, zwnoded_p node, cJSON *Node)
{
    ext_ver_t   *ext_ver;
    int         i;
    char str[50] = {0};

    ext_ver = zwnode_get_ext_ver(node);
    if (ext_ver)
    {
        plt_msg_show(hl_plt_ctx_get(hl_appl), "Hardware version:%u", (unsigned)(ext_ver->hw_ver));
        cJSON_AddNumberToObject(Node, "Hardware version", ext_ver->hw_ver);

        for (i=0; i<ext_ver->fw_cnt; i++)
        {
            plt_msg_show(hl_plt_ctx_get(hl_appl), "Firmware %d version:%u.%02u", i+1, (unsigned)(ext_ver->fw_ver[i] >> 8),
                         (unsigned)(ext_ver->fw_ver[i] & 0xFF));

            cJSON_AddNumberToObject(Node, "Firmware", i+1);
            sprintf(str, "%u.%02u", ext_ver->fw_ver[i] >> 8, ext_ver->fw_ver[i] & 0xFF);
            cJSON_AddStringToObject(Node, "Firmware version", str);
        }
        free(ext_ver);
    }
}

/**
hl_dev_id_show - Show device id
@param[in]	hl_appl   The high-level api context
@param[in]	node	  Node
@return
*/
static void hl_dev_id_show(hl_appl_ctx_t *hl_appl, dev_id_t *dev_id)
{
    const char *dev_id_type_str[] =
            {
                    "Device id oem",
                    "Device serial number",
                    "Device id unknown type"
            };
    uint8_t   id_type;

    id_type = (uint8_t)((dev_id->type > DEV_ID_TYPE_SN)? 2 : dev_id->type);

    if (dev_id->format == DEV_ID_FMT_UTF)
    {   //UTF-8
        plt_msg_show(hl_plt_ctx_get(hl_appl), "%s:%s", dev_id_type_str[id_type], dev_id->dev_id);
    }
    else if (dev_id->format == DEV_ID_FMT_BIN)
    {   //Binary
        char hex_string[(32*3)+1];

        hl_bin2str(dev_id->dev_id, dev_id->len, hex_string, (32*3)+1);
        plt_msg_show(hl_plt_ctx_get(hl_appl), "%s:h'%s", dev_id_type_str[id_type], hex_string);
    }
}

/**
hl_zwaveplus_show - Show Z-Wave+ information
@param[in]	hl_appl   The high-level api context
@param[in]	info	  Z-wave+ information
@return
*/
static void hl_zwaveplus_show(hl_appl_ctx_t *hl_appl, zwplus_info_t *info, cJSON *EpInfo)
{
    int         idx;
    const char *zwplus_node_type_str[] =
            {
                    "Z-Wave+ node",
                    "Z-Wave+ for IP router",
                    "Z-Wave+ for IP gateway",
                    "Z-Wave+ for IP - client IP node",
                    "Z-Wave+ for IP - client Z-Wave node",
                    "unknown"
            };

    const char *zwplus_role_type_str[] =
            {
                    "Central Static Controller",
                    "Sub Static Controller",
                    "Portable Controller",
                    "Portable Reporting Controller",
                    "Portable Slave",
                    "Always On Slave",
                    "Sleeping Reporting Slave",
                    "Reachable_Sleeping_Slave",
                    "unknown"
            };

    plt_msg_show(hl_plt_ctx_get(hl_appl), "ZWave+ version:%u", (unsigned)(info->zwplus_ver));
    cJSON_AddNumberToObject(EpInfo, "ZWave+ version", info->zwplus_ver);

    idx = (info->node_type <= 4)? info->node_type : 5;
    plt_msg_show(hl_plt_ctx_get(hl_appl), "ZWave+ node type:%s", zwplus_node_type_str[idx]);
    cJSON_AddStringToObject(EpInfo, "ZWave+ node type", zwplus_node_type_str[idx]);

    idx = (info->role_type <= 7)? info->role_type : 8;
    plt_msg_show(hl_plt_ctx_get(hl_appl), "ZWave+ role type:%s", zwplus_role_type_str[idx]);
    cJSON_AddStringToObject(EpInfo, "ZWave+ role type", zwplus_role_type_str[idx]);

    char str[50] = {0};

    plt_msg_show(hl_plt_ctx_get(hl_appl), "ZWave+ installer icon:%04Xh", (unsigned)(info->instr_icon));
    sprintf(str, "%04Xh", info->instr_icon);
    cJSON_AddStringToObject(EpInfo, "ZWave+ installer icon", str);

    plt_msg_show(hl_plt_ctx_get(hl_appl), "ZWave+ user icon:%04Xh", (unsigned)(info->usr_icon));
    sprintf(str, "%04Xh", info->usr_icon);
    cJSON_AddStringToObject(EpInfo, "ZWave+ user icon", str);
}

/**
hl_sup_sensor_show - Show supported sensor types and units
@param[in]	intf	  Multilevel sensor interface
@return
*/
static void  hl_sup_sensor_show(zwifd_p intf, cJSON *interfaceInfo)
{
    int                 i;
    int                 j;
    uint8_t             type;
    uint8_t             sensor_cnt;
    if_sensor_data_t    *sup_sensor;
    const char          **unit_str;

    if (zwif_sensor_sup_cache_get(intf, &sup_sensor, &sensor_cnt) != 0)
    {
        return;
    }

    cJSON * sensorInfoArray =  cJSON_CreateArray();

    if(sensorInfoArray == NULL)
    {
        return;
    }

    cJSON_AddItemToObject(interfaceInfo, "Sensor Info List", sensorInfoArray);

    for (i=0; i<sensor_cnt; i++)
    {
        cJSON *sensorInfo = cJSON_CreateObject();

        if(sensorInfo == NULL)
        {
            return;
        }

        cJSON_AddItemToArray(sensorInfoArray, sensorInfo);

        type = sup_sensor[i].sensor_type;

        if (type > ZW_SENSOR_TYPE_TGT_TEMP)
        {
            type = 0;
        }

        plt_msg_show(hl_if_plt_ctx_get(intf), "                        Supported sensor type:%s, sensor units:", sensor_type_str[type]);

        cJSON_AddStringToObject(sensorInfo, "sensor type", sensor_type_str[type]);

        cJSON * unitArray =  cJSON_CreateArray();

        if(unitArray == NULL)
        {
            return;
        }

        cJSON_AddItemToObject(sensorInfo, "Unit List", unitArray);

        for (j=0; j<4; j++)
        {
            if (sup_sensor[i].sensor_unit & (0x01 << j))
            {
                unit_str = sensor_unit_str + (type * 4) + j;

                if (*unit_str == NULL)
                {
                    *unit_str = "undefined";
                }

                plt_msg_show(hl_if_plt_ctx_get(intf), "                                                         %s", *unit_str);

                cJSON_AddStringToObject(unitArray, "unit", *unit_str);
            }
        }
    }
}

/**
hl_node_desc_dump - dump the node descriptor info
@param[in]	hl_appl		The high-level api context
@return
*/
static int hl_node_desc_dump(hl_appl_ctx_t *hl_appl, cJSON *jsonRoot)
{
    int         result;
    zwnetd_p    net_desc;
    zwnoded_p   node;
    zwepd_p     ep;
    zwifd_p     intf;
    desc_cont_t *last_node_cont;
    desc_cont_t *last_ep_cont;
    desc_cont_t *last_intf_cont;
    char str[100] = {0};

    if(jsonRoot == NULL)
    {
        return -1;
    }

    plt_mtx_lck(hl_appl->desc_cont_mtx);

    //Check whether the descriptor container linked list is initialized
    if (!hl_appl->desc_cont_hd)
    {
        result = hl_desc_init(&hl_appl->desc_cont_hd, hl_appl->zwnet);
        if (result != 0)
        {
            plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_desc_init with error:%d", result);
            return result;
        }
    }

    cJSON * NodeInfoArray = cJSON_CreateArray();

    if(NodeInfoArray == NULL)
    {
        return -1;
    }

    cJSON_AddItemToObject(jsonRoot, "Node Info List", NodeInfoArray);

    //Get the first node (local controller) and home id
    last_node_cont = hl_appl->desc_cont_hd;

    net_desc = zwnet_get_desc(hl_appl->zwnet);

    while (last_node_cont)
    {
        cJSON *NodeInfo = cJSON_CreateObject();

        if(NodeInfo == NULL)
        {
            return -1;
        }

        cJSON_AddItemToArray(NodeInfoArray, NodeInfo);

        if (last_node_cont->type != DESC_TYPE_NODE)
        {
            plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "node: wrong desc type:%u", last_node_cont->type);
        }

        node = (zwnoded_p)last_node_cont->desc;

        plt_msg_show(hl_plt_ctx_get(hl_appl), "__________________________________________________________________________");
        plt_msg_show(hl_plt_ctx_get(hl_appl), "Node id:%u[%u], Home id:%08X", (unsigned)node->nodeid,
                     last_node_cont->id, (unsigned)net_desc->id);
        plt_msg_show(hl_plt_ctx_get(hl_appl), "Node status:%s", (node->alive)?  "alive" : "down/sleeping");

        sprintf(str, "%08X", net_desc->id);
        cJSON_AddStringToObject(NodeInfo, "Home id", str);
        cJSON_AddNumberToObject(NodeInfo, "Node id", node->nodeid);
        cJSON_AddStringToObject(NodeInfo, "Node status", (node->alive)?  "alive" : "down/sleeping");

        if (node->sleep_cap)
        {
            plt_msg_show(hl_plt_ctx_get(hl_appl), "Node is capable to sleep with wakeup interval:%us", node->wkup_intv);

            sprintf(str, "%us", node->wkup_intv);
            cJSON_AddStringToObject(NodeInfo, "wakeup interval", str);
        }

        if (node->sensor)
        {
            plt_msg_show(hl_plt_ctx_get(hl_appl), "Node is FLIRS");
        }

        plt_msg_show(hl_plt_ctx_get(hl_appl), "Node security inclusion status:%s", (node->sec_incl_failed)?  "failed" : "unknown");
        plt_msg_show(hl_plt_ctx_get(hl_appl), "Vendor id:%04X", node->vid);
        plt_msg_show(hl_plt_ctx_get(hl_appl), "Product type id:%04X", node->type);
        plt_msg_show(hl_plt_ctx_get(hl_appl), "Product id:%04X", node->pid);
        plt_msg_show(hl_plt_ctx_get(hl_appl), "Category:%s", (node->category <= DEV_WALL_CTLR)?
                                                             dev_category_str[node->category] : "unknown");
        plt_msg_show(hl_plt_ctx_get(hl_appl), "Z-wave library type:%u", node->lib_type);
        plt_msg_show(hl_plt_ctx_get(hl_appl), "Z-wave protocol version:%u.%02u\n", (unsigned)(node->proto_ver >> 8),
                     (unsigned)(node->proto_ver & 0xFF));
        plt_msg_show(hl_plt_ctx_get(hl_appl), "Application version:%u.%02u\n", (unsigned)(node->app_ver >> 8),
                     (unsigned)(node->app_ver & 0xFF));

        cJSON_AddStringToObject(NodeInfo, "Node security inclusion status", (node->sec_incl_failed)?  "failed" : "unknown");

        sprintf(str, "%04X", node->vid);
        cJSON_AddStringToObject(NodeInfo, "Vendor id", str);

        sprintf(str, "%04X", node->type);
        cJSON_AddStringToObject(NodeInfo, "Product type id", str);

        sprintf(str, "%04X", node->pid);
        cJSON_AddStringToObject(NodeInfo, "Product id", str);

        cJSON_AddStringToObject(NodeInfo, "Category", (node->category <= DEV_WALL_CTLR)?
                                                  dev_category_str[node->category] : "unknown");
        cJSON_AddNumberToObject(NodeInfo, "Z-wave library type", node->lib_type);

        sprintf(str, "%u.%02u", node->proto_ver >> 8, node->proto_ver & 0xFF);
        cJSON_AddStringToObject(NodeInfo, "Z-wave protocol version", str);

        sprintf(str, "%u.%02u", node->app_ver >> 8, node->app_ver & 0xFF);
        cJSON_AddStringToObject(NodeInfo, "Application version", str);

        hl_ext_ver_show(hl_appl, node, NodeInfo);

        if (node->dev_id.len > 0)
        {
            hl_dev_id_show(hl_appl, &node->dev_id);
        }

        cJSON * EpInfoArray =  cJSON_CreateArray();

        if(EpInfoArray == NULL)
        {
            return -1;
        }

        cJSON_AddItemToObject(NodeInfo, "EndPoint List", EpInfoArray);

        //Get endpoint
        last_ep_cont = last_node_cont->down;

        while (last_ep_cont)
        {
            cJSON *EpInfo = cJSON_CreateObject();

            if(EpInfo == NULL)
            {
                return -1;
            }

            cJSON_AddItemToArray(EpInfoArray, EpInfo);

            if (last_ep_cont->type != DESC_TYPE_EP)
            {
                plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "ep: wrong desc type:%u", last_ep_cont->type);
            }

            ep = (zwepd_p)last_ep_cont->desc;

            plt_msg_show(hl_plt_ctx_get(hl_appl), "Endpoint id:%u[%u]", ep->epid, last_ep_cont->id);
            plt_msg_show(hl_plt_ctx_get(hl_appl), "Device class: generic:%02X, specific:%02X",
                         ep->generic, ep->specific);
            plt_msg_show(hl_plt_ctx_get(hl_appl), "Endpoint name:%s", ep->name);
            plt_msg_show(hl_plt_ctx_get(hl_appl), "Endpoint location:%s", ep->loc);

            cJSON_AddNumberToObject(EpInfo, "Endpoint id", ep->epid);

            sprintf(str, "%02X", ep->generic);
            cJSON_AddStringToObject(EpInfo, "Device class generic", str);

            sprintf(str, "%02X", ep->specific);
            cJSON_AddStringToObject(EpInfo, "Device class specific", str);
            cJSON_AddStringToObject(EpInfo, "Endpoint name", ep->name);
            cJSON_AddStringToObject(EpInfo, "Endpoint location", ep->loc);

            if (ep->zwplus_info.zwplus_ver)
            {
                hl_zwaveplus_show(hl_appl, &ep->zwplus_info, EpInfo);
            }

            cJSON * InterfaceInfoArray =  cJSON_CreateArray();

            if(InterfaceInfoArray == NULL)
            {
                return -1;
            }

            cJSON_AddItemToObject(EpInfo, "Interface List", InterfaceInfoArray);

            //Get interface
            last_intf_cont = last_ep_cont->down;

            while (last_intf_cont)
            {
                cJSON *InterfaceInfo = cJSON_CreateObject();

                if(InterfaceInfo == NULL)
                {
                    return -1;
                }

                cJSON_AddItemToArray(InterfaceInfoArray, InterfaceInfo);

                if (last_intf_cont->type != DESC_TYPE_INTF)
                {
                    plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "interface: wrong desc type:%u", last_intf_cont->type);
                }

                intf = (zwifd_p)last_intf_cont->desc;

                plt_msg_show(hl_plt_ctx_get(hl_appl), "              Interface: %02Xv%u:%s [%u]%c%c",
                             (unsigned)intf->cls, intf->ver, hl_class_str_get(intf->cls, intf->ver),
                             last_intf_cont->id, (intf->propty & IF_PROPTY_SECURE)? '*' : ' ',
                             (intf->propty & IF_PROPTY_UNSECURE)? '^' : ' ');

                cJSON_AddStringToObject(InterfaceInfo, "Interface Class", hl_class_str_get(intf->cls, intf->ver));
                cJSON_AddNumberToObject(InterfaceInfo, "Interface Id", last_intf_cont->id);

                if (intf->cls == COMMAND_CLASS_SENSOR_MULTILEVEL)
                {
                    hl_sup_sensor_show(intf, InterfaceInfo);
                }
                else if (intf->cls == COMMAND_CLASS_ASSOCIATION_GRP_INFO)
                {
                    hl_grp_info_show(intf, InterfaceInfo);
                }

                //Get the next interface
                last_intf_cont = last_intf_cont->next;
            }

            //Get the next endpoint
            last_ep_cont = last_ep_cont->next;
        }

        //Get the next node
        last_node_cont = last_node_cont->next;
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    return 0;
}

int zwcontrol_init(hl_appl_ctx_t *hl_appl, const char *cfgfile, const char* filepath, const char* infopath, uint8_t* result)
{
    int         ret;
#ifndef OS_MAC_X
    unsigned    port_num;
#endif

    if (hl_appl->is_init_done)
        return 0;
		
	if(infopath == NULL)
	{
		return -1;
	}

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return -1;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Controller Attribute");

    //Clear application context
    memset(hl_appl, 0, sizeof(hl_appl_ctx_t));

    if(access(infopath,F_OK) == 0)
    {
        hl_appl->load_ni_file = 1;
        hl_appl->save_ni_file = 1;
        ALOGD("nodeinfo file exists, will load it.");
        strcpy(hl_appl->save_file,infopath);
        strcpy(hl_appl->node_info_file,infopath);
    }else{
        ALOGD("nodeinfo file not exists, first init");
    }

    int num = 1;

    //Initialize
    Again:
     ret = hl_init(hl_appl, cfgfile, filepath, jsonRoot);

    if (ret < 0)
    {
        ALOGE("hl_init with error: %d\n", ret);

        if(ret == ZW_ERR_COMM) {

            ++num;

            if (num <= 2)
                goto Again;
        }
    }
    else
    {
        if(result != NULL)
        {
            char *p = cJSON_Print(jsonRoot);

            if(p != NULL)
            {
                strcpy((char*)result, p);
                free(p);
            }
        }
    }

    cJSON_Delete(jsonRoot);

    return ret;
}

void zwcontrol_setcallback(ResCallBack callBack)
{
    resCallBack = callBack;
}

void zwcontrol_exit(hl_appl_ctx_t *hl_appl)
{
    if (hl_appl->is_init_done)
    {
        hl_close(hl_appl);
    }
}

int  zwcontrol_add_node(hl_appl_ctx_t *hl_appl)
{
    if (!hl_appl->is_init_done){
        ALOGE("Controller not open, please open it and try again");
        return -1;
    }

    ALOGD("Controller add node");
    int result;
    result = zwnet_add(hl_appl->zwnet, 1);

    if (result != 0)
    {
        ALOGE("zwcontrol_add_node with error:%d", result);
    }

    return result;
}

int  zwcontrol_rm_node(hl_appl_ctx_t *hl_appl)
{
    if (!hl_appl->is_init_done){
        ALOGE("Controller not open, please open it and try again");
        return -1;
    }

    ALOGD("Controller remove node");
    int result;
    result = zwnet_add(hl_appl->zwnet, 0);

    if (result != 0)
    {
        ALOGE("zwcontrol_add_node with error:%d", result);
    }

    return result;
}

int  zwcontrol_get_node_list(hl_appl_ctx_t *hl_appl)
{
    if (!hl_appl->is_init_done){
        ALOGE("Controller not open, please open it and try again");
        return -1;
    }

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return -1;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Node List Report");

    int res =  hl_node_desc_dump(hl_appl, jsonRoot);

    if(res == 0)
    {
        char *p = cJSON_Print(jsonRoot);

        if(p != NULL)
        {
            if(resCallBack)
            {
                resCallBack(p);
            }

            free(p);
        }
    }

    cJSON_Delete(jsonRoot);

    return res;
}

int  zwcontrol_rm_failed_node(hl_appl_ctx_t *hl_appl, uint32_t nodeId)
{
    if (!hl_appl->is_init_done){
        ALOGE("Controller not open, please open it and try again");
        return -1;
    }

    ALOGD("Remove failed node, id %d",nodeId);
    hl_appl->failed_node_id = nodeId;
    int32_t   result;
    zwnoded_p noded;

    //Get the node descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    noded = hl_node_desc_get(hl_appl->desc_cont_hd, hl_appl->failed_node_id);
    if (!noded)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_NODE_NOT_FOUND;
    }

    result = zwnet_fail(noded, 0);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_failed_id_rm with error:%d", result);
    }

    return result;
}

int  zwcontrol_rp_failed_node(hl_appl_ctx_t *hl_appl, uint32_t nodeId)
{
    if (!hl_appl->is_init_done){
        ALOGE("Controller not open, please open it and try again");
        return -1;
    }

    ALOGD("Replace failed node, id %d",nodeId);
    hl_appl->failed_node_id = nodeId;
    int32_t     result;
    zwnoded_p noded;

    //Get the node descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    noded = hl_node_desc_get(hl_appl->desc_cont_hd, hl_appl->failed_node_id);
    if (!noded)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_NODE_NOT_FOUND;
    }

    result = zwnet_fail(noded, 1);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_failed_id_rm with error:%d", result);
    }

    return result;
}

int zwcontrol_stop_op(hl_appl_ctx_t *hl_appl)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    return zwnet_abort(hl_appl->zwnet);
}

/**
hl_deflt_set - Restore factory default
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
static int32_t hl_deflt_set(hl_appl_ctx_t   *hl_appl)
{
    int result;

    result = zwnet_reset(hl_appl->zwnet);
    if (result != 0)
    {
        ALOGE("controller default set with error:%d", result);
    }
    return result;
}

int  zwcontrol_default_set(hl_appl_ctx_t *hl_appl)
{
    if (!hl_appl->is_init_done){
        ALOGE("Controller not open, please open it and try again");
        return -1;
    }

    ALOGD("controller set default");
    if(remove(hl_appl->save_file) == 0)
    {
        ALOGD("remove NodeInfo file successfully");
    } else{
        ALOGE("remove NodeInfo file error");
    }
    if (hl_appl->is_init_done)
    {
        return hl_deflt_set(hl_appl);
    }

    return -1;
}

/**
hl_desc_cont_get - get the descriptor container for the specified id
@param[in]  head        The head of the descriptor container linked-list
@param[in]  desc_id         Unique descriptor id
@return     Descriptor container if found; else return NULL
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
static desc_cont_t    *hl_desc_cont_get(desc_cont_t *head, uint32_t desc_id)
{
    desc_cont_t     *last_node_cont;
    desc_cont_t     *last_ep_cont;
    desc_cont_t     *last_cls_cont;
    desc_cont_t     *last_intf_cont;

    //Start searching from the first node
    last_node_cont = head;

    while (last_node_cont)
    {
        if (last_node_cont->id == desc_id)
        {
            return last_node_cont;
        }

        //Search endpoint
        last_ep_cont = last_node_cont->down;

        while (last_ep_cont)
        {
            if (last_ep_cont->id == desc_id)
            {
                return last_ep_cont;
            }

            //Search class
            last_cls_cont = last_ep_cont->down;

            while (last_cls_cont)
            {
                if (last_cls_cont->id == desc_id)
                {
                    return last_cls_cont;
                }

                //Search interface
                last_intf_cont = last_cls_cont->down;

                while (last_intf_cont)
                {
                    if (last_intf_cont->id == desc_id)
                    {
                        return last_intf_cont;
                    }
                    //Get the next interface
                    last_intf_cont = last_intf_cont->next;
                }
                //Get the next class
                last_cls_cont = last_cls_cont->next;
            }
            //Get the next endpoint
            last_ep_cont = last_ep_cont->next;
        }
        //Get the next node
        last_node_cont = last_node_cont->next;
    }
    return NULL;
}

/**
hl_intf_desc_get - get interface descriptor from descriptor container
@param[in]  head        The head of the descriptor container linked-list
@param[in]  desc_id     Unique descriptor id
@return     Interface descriptor if found; else return NULL
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
zwifd_p    hl_intf_desc_get(desc_cont_t *head, uint32_t desc_id)
{
    desc_cont_t *desc_cont;

    //Get the interface descriptor
    desc_cont = hl_desc_cont_get(head, desc_id);
    if (!desc_cont)
    {
        ALOGD("hl_intf_desc_get invalid desc id:%u\n",desc_id);
        //plt_msg_ts_show("hl_intf_desc_get invalid desc id:%u", desc_id);
        return NULL;
    }

    if (desc_cont->type != DESC_TYPE_INTF)
    {
        ALOGD("hl_intf_desc_get desc id:%u is not type interface\n", desc_id);
        //plt_msg_ts_show("hl_intf_desc_get desc id:%u is not type interface", desc_id);
        return NULL;
    }

    return(zwifd_p)desc_cont->desc;
}

/**
hl_battery_report_cb - battery command report callback
@param[in]  ifd     The interface that received the report
@param[in]  value   The value
@return
*/
void hl_battery_report_cb(zwifd_p ifd, uint8_t value)
{
    if (/*(value >= 0) &&*/ (value <= 100))
    {
        ALOGD("Battery level is %u%%", value);
    }
    else if (value == 0xFF)
    {
        ALOGD("Battery low warning!");
    }

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Node Battery Value");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddNumberToObject(jsonRoot, "EndPoint Id", ifd->epid);
    cJSON_AddNumberToObject(jsonRoot, "Battery Value", value);

    if(resCallBack)
    {
        char *p = cJSON_Print(jsonRoot);

        if(p != NULL)
        {
            resCallBack(p);
            free(p);
        }
    }

    cJSON_Delete(jsonRoot);
}

/**
hl_battery_rep_setup - Setup battery command report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_battery_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_battery_rpt_set(ifd, hl_battery_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGD("hl_battery_rep_setup with error:%d", result);
    }

    return result;
}

/**
hl_battery_rep_get - Get battery command report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_battery_rep_get(hl_appl_ctx_t   *hl_appl)
{
    //hl_appl->rep_desc_id = prompt_uint("Enter desc id of the report:");
    ALOGD("battery report get,hl_appl->dst_desc_id= %d",hl_appl->dst_desc_id);
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    if (hl_appl->poll_ctl)
    {
        result = zwif_battery_get_poll(ifd, &hl_appl->poll_req);
        if (result == 0)
        {
            ALOGD("Polling request handle:%u", hl_appl->poll_req.handle);
        }
    }
    else
    {
        result = zwif_battery_get(ifd);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGD("hl_battery_rep_get with error:%d", result);
    }

    return result;
}

int zwcontrol_battery_get(hl_appl_ctx_t *hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_BATTERY))
    {
        return -1;
    }

    int result = hl_battery_rep_setup(hl_appl);

    if(result == 0)
    {
        ALOGD("battery report setup done");
        result = hl_battery_rep_get(hl_appl);
    }

    return result;
}

/**
hl_int_get - get integer value from a byte stream
@param[in]  byte_buf    The buffer that holds the bytes
@param[in]  size        The integer size
@param[out] int_val     The result of the conversion from bytes to integer
@return     1 on success; else return 0
*/
int  hl_int_get(uint8_t *byte_buf,  uint8_t size, int32_t *int_val)
{
    if (size == 1)
    {
        int8_t  val8;

        val8 = byte_buf[0];
        *int_val = val8;
        return 1;
    }
    else if (size == 2)
    {
        int16_t val16;

        val16 = byte_buf[0];
        val16 = (val16 << 8) | byte_buf[1];
        *int_val = val16;
        return 1;
    }
    else
    {
        int32_t val32;

        val32 = ((int32_t)(byte_buf[0])) << 24
                | ((int32_t)(byte_buf[1])) << 16
                | ((int32_t)(byte_buf[2])) << 8
                | byte_buf[3];

        *int_val = val32;
        return 1;
    }
    return 0;
}

/**
hl_float_get - get floating point integer string from an integer with specified precision
@param[in]  int_value    Integer value
@param[in]  precision    Number of decimal points
@param[in]  buf_size     Output buffer size
@param[out] out_buf      Output buffer that contains the converted string.
@return
*/
void  hl_float_get(int32_t int_value,  uint8_t precision, uint8_t buf_size, char *out_buf)
{
    int i;
    int32_t   divisor;
    int32_t   whole_num;
    int32_t   dec_num;
    char      format_str[20];

    divisor = 1;
    for (i=0 ; i < precision; i++)
    {
        divisor *= 10;
    }

    whole_num = int_value / divisor;

    if (int_value < 0)
    {
        dec_num = (int_value * (-1)) % divisor;
    }
    else
    {
        dec_num = int_value % divisor;
    }

    if (precision > 0)
    {
        sprintf(format_str, "%%d.%%.%ud", precision);
        sprintf(out_buf, format_str, whole_num, dec_num);
    }
    else
    {
        sprintf(out_buf, "%d", int_value);
    }
}

/**
hl_ml_snsr_rep_cb - multi-level sensor report callback
@param[in]  ifd interface
@param[in]  value       The current value and unit of the sensor.
*/
static void hl_ml_snsr_rep_cb(zwifd_p ifd, zwsensor_t *value)
{
    int32_t         snsr_value;
    const char      **unit_str;

    if (value->type > ZW_SENSOR_TYPE_TGT_TEMP)
    {
        value->type = 0;
    }

    unit_str = sensor_unit_str + (value->type * 4) + value->unit;

    if (*unit_str == NULL)
    {
        *unit_str = "undefined";
    }

    ALOGD("Multi-level sensor report, type:%s, precision:%u, unit:%s",
                    sensor_type_str[value->type], value->precision, *unit_str);

    if (!hl_int_get(value->data, value->size, &snsr_value))
    {   //Error, default to zero
        snsr_value = 0;
    }

    char float_str[80] = {0};

    if (value->precision == 0)
    {
        ALOGD("Sensor reading:%d", snsr_value);
    }
    else
    {
        hl_float_get(snsr_value, value->precision, 80, float_str);
        ALOGD("Sensor reading:%s", float_str);
    }

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Sensor Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddStringToObject(jsonRoot, "type", sensor_type_str[value->type]);
    cJSON_AddNumberToObject(jsonRoot, "precision", value->precision);
    cJSON_AddStringToObject(jsonRoot, "unit", *unit_str);
    cJSON_AddStringToObject(jsonRoot, "value", float_str);

    if(resCallBack)
    {
        char *p = cJSON_Print(jsonRoot);

        if(p)
        {
            resCallBack(p);
            free(p);
        }
    }

    cJSON_Delete(jsonRoot);
}

/**
hl_ml_snsr_rep_setup - Setup multi-level sensor report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_ml_snsr_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    ALOGD("Setup multi-level sensor report, id %d\n", hl_appl->rep_desc_id);
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        ALOGD("report setup, interface not found\n");
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_sensor_rpt_set(ifd, hl_ml_snsr_rep_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGD("hl_bin_snsr_rep_setup with error:%d", result);
    }

    return result;
}

/**
hl_ml_snsr_rep_get - Get multi-level sensor state report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_ml_snsr_rep_get(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    if (hl_appl->poll_ctl)
    {
        result = zwif_sensor_get_poll(ifd, hl_appl->sensor_type, hl_appl->sensor_unit, &hl_appl->poll_req);
        if (result == 0)
        {
            ALOGD("Polling request handle:%u", hl_appl->poll_req.handle);
        }
    }
    else
    {
        result = zwif_sensor_get(ifd, hl_appl->sensor_type, hl_appl->sensor_unit);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGD("hl_ml_snsr_rep_get with error:%d", result);
    }

    return result;
}

/**
hl_ep_desc_get - get endpoint descriptor from descriptor container
@param[in]	head	    The head of the descriptor container linked-list
@param[in]	desc_id		Unique descriptor id
@return     Endpoint descriptor if found; else return NULL
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
zwepd_p    hl_ep_desc_get(desc_cont_t *head, uint32_t desc_id)
{
    desc_cont_t *desc_cont;

    //Get the interface descriptor
    desc_cont = hl_desc_cont_get(head, desc_id);
    if (!desc_cont)
    {
        //plt_msg_ts_show("hl_ep_desc_get invalid desc id:%u", desc_id);
        return NULL;
    }

    if (desc_cont->type != DESC_TYPE_EP)
    {
        //plt_msg_ts_show("hl_ep_desc_get desc id:%u is not type endpoint", desc_id);
        return NULL;
    }

    return(zwepd_p)desc_cont->desc;
}

/**
hl_ep_nameloc_set - Set name & location of an endpoint
@param[in]	hl_appl		        The high-level api context
@param[in]	ep_desc_id		    Endpoint descriptor id
@return  0 on success, negative error number on failure
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
int hl_ep_nameloc_set(hl_appl_ctx_t   *hl_appl, uint32_t ep_desc_id)
{
    int32_t     result;
    zwepd_p     epd;

    //Get the endpoint descriptor
    epd = hl_ep_desc_get(hl_appl->desc_cont_hd, ep_desc_id);
    if (!epd)
    {
        return ZW_ERR_EP_NOT_FOUND;
    }

    result = zwep_nameloc_set(epd, &hl_appl->nameloc);

    if (result == 0)
    {
        //Update the endpoint descriptor
#ifdef USE_SAFE_VERSION
        strcpy_s(epd->name, ZW_LOC_STR_MAX + 1, hl_appl->nameloc.name);
        strcpy_s(epd->loc, ZW_LOC_STR_MAX + 1, hl_appl->nameloc.loc);
#else
        strcpy(epd->name, hl_appl->nameloc.name);
        strcpy(epd->loc, hl_appl->nameloc.loc);
#endif
    }
    else
    {
        plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_ep_nameloc_set with error:%d", result);

    }
    return result;
}

/**
hl_nameloc_set - Set name & location of a  node id
@param[in]	hl_appl		The high-level api context
@return  0 on success, negative error number on failure
*/
static int32_t hl_nameloc_set(hl_appl_ctx_t   *hl_appl)
{
    int         result;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);

    result = hl_ep_nameloc_set(hl_appl, hl_appl->rep_desc_id);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result < 0)
    {
        plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_nameloc_set with error:%d", result);
    }

    return result;
}

int zwcontrol_sensor_multilevel_get(hl_appl_ctx_t *hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SENSOR_MULTILEVEL))
    {
        return -1;
    }

    int result = hl_ml_snsr_rep_setup(hl_appl);

    if(result == 0)
    {
        ALOGD("sensor report setup done.");
        
        result = hl_ml_snsr_rep_get(hl_appl);
    }

    return result;
}

int zwcontrol_update_node(hl_appl_ctx_t *hl_appl, uint8_t nodeId)
{
    ALOGD("zwcontrol_update_node, nodeId:%d",nodeId);
    int         result, find = 0;
    zwnoded_p   node;
    desc_cont_t *last_node_cont;

    plt_mtx_lck(hl_appl->desc_cont_mtx);

    //Check whether the descriptor container linked list is initialized
    if (!hl_appl->desc_cont_hd)
    {
        result = hl_desc_init(&hl_appl->desc_cont_hd, hl_appl->zwnet);
        if (result != 0)
        {
            plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "zwcontrol_update_node with error:%d", result);
            plt_mtx_ulck(hl_appl->desc_cont_mtx);
            return -1;
        }
    }

    //Get the first node (local controller) and home id
    last_node_cont = hl_appl->desc_cont_hd;

    while (last_node_cont)
    {
        if (last_node_cont->type != DESC_TYPE_NODE)
        {
            plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "node: wrong desc type:%u", last_node_cont->type);
        }

        node = (zwnoded_p)last_node_cont->desc;

        if(node->nodeid == nodeId)
        {
            hl_appl->dst_desc_id = last_node_cont->id;
            find = 1;
            break;
        }

        last_node_cont = last_node_cont->next;
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    result = -1;

    if(find == 1)
    {
        result = hl_node_updt(hl_appl);
    }

    return result;
}

/**
hl_save - Save settings into a file
@param[in]  hl_appl     The high-level api context
@return
*/
int    hl_save(hl_appl_ctx_t   *hl_appl)
{
    int result;

    result = zwnet_save(hl_appl->zwnet, hl_appl->save_file);
    if (result != 0)
    {
        ALOGE("save NodeInfo with error:%d", result);
    }
    if(result == 0)
        ALOGD("save nodeinfo successfully");
    return result;
}

int  zwcontrol_save_nodeinfo(hl_appl_ctx_t *hl_appl, const char* filepath)
{
    ALOGD("zwcontrol_save_nodeinfo started");
    if (hl_appl->is_init_done)
    {
        strcpy(hl_appl->save_file,filepath);
        //hl_appl->save_file = "zwController_nodeInfo.txt";
        return hl_save(hl_appl);
    }
    return -1;
}

/**
hl_basic_report_cb - basic command report callback
@param[in]	ifd	    The interface that received the report
@param[in]	value	The value
@return
*/
void hl_basic_report_cb(zwifd_p ifd, uint8_t value)
{
    plt_msg_ts_show(hl_if_plt_ctx_get(ifd), "Basic command value is %02Xh", value);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Basic Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    char str[50] = {0};
    sprintf(str, "%02Xh", value);
    cJSON_AddStringToObject(jsonRoot, "value", str);
    
    if(resCallBack)
    {
        char *p = cJSON_Print(jsonRoot);

        if(p)
        {
            resCallBack(p);
            free(p);
        }
    }

    cJSON_Delete(jsonRoot);
}

/**
hl_basic_rep_setup - Setup basic command report
@param[in]	hl_appl		The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_basic_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_basic_rpt_set(ifd, hl_basic_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_basic_rep_setup with error:%d", result);
    }

    return result;
}

/**
hl_basic_rep_get - Get basic command report
@param[in]	hl_appl		The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_basic_rep_get(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    if (hl_appl->poll_ctl)
    {
        result = zwif_basic_get_poll(ifd, &hl_appl->poll_req);
        if (result == 0)
        {
            plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "Polling request handle:%u", hl_appl->poll_req.handle);
        }
    }
    else
    {
        result = zwif_basic_get(ifd);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_basic_rep_get with error:%d", result);
    }

    return result;
}

int zwcontrol_basic_get(hl_appl_ctx_t *hl_appl, int nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_BASIC))
    {
        return -1;
    }

    int result = hl_basic_rep_setup(hl_appl);

    if(result == 0)
    {
        result = hl_basic_rep_get(hl_appl);
    }

    return result;
}

/**
hl_basic_set - basic command set value
@param[in]	hl_appl		The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_basic_set(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_basic_set(ifd, (uint8_t)hl_appl->basic_val);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_basic_set with error:%d", result);
    }

    return result;

}

int  zwcontrol_basic_set(hl_appl_ctx_t *hl_appl, int nodeId, int value)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_BASIC))
    {
        return -1;
    }

    hl_appl->basic_val = (uint16_t)value;

    int result = hl_basic_set(hl_appl);

    return result;
}

/**
hl_generic_report_cb - Generic report callback
@param[in]  ifd The interface that received the report
@param[in]  level       The reported level
@return
*/
void hl_generic_report_cb(zwifd_p ifd, uint8_t level)
{
    ALOGI("Generic report level:%02Xh", level);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Generic Report Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    char str[50] = {0};
    sprintf(str, "%02Xh", level);
    cJSON_AddStringToObject(jsonRoot, "level", str);

    if(resCallBack)
    {
        char *p = cJSON_Print(jsonRoot);

        if(p)
        {
            resCallBack(p);
            free(p);
        }
    }

    cJSON_Delete(jsonRoot);
}

/**
hl_multi_lvl_rep_setup - Setup multi-level switch report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_multi_lvl_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);

    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_level_rpt_set(ifd, hl_generic_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_multi_lvl_rep_setup with error:%d", result);
    }

    return result;
}

/**
hl_multi_lvl_rep_get - Get multi-level switch level report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_multi_lvl_rep_get(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);

    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    if (hl_appl->poll_ctl)
    {
        result = zwif_level_get_poll(ifd, &hl_appl->poll_req);
        if (result == 0)
        {
            ALOGI("Polling request handle:%u", hl_appl->poll_req.handle);
        }
    }
    else
    {
        result = zwif_level_get(ifd);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGI("hl_multi_lvl_rep_get with error:%d", result);
    }

    return result;
}

int zwcontrol_switch_multilevel_get(hl_appl_ctx_t* hl_appl, int nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    ALOGI("zwcontroller_switch_multilevel_get started");
    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_MULTILEVEL))
    {
        return -1;
    }

    int result = hl_multi_lvl_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGD("switch multilevel report setup done.");
        
        result = hl_multi_lvl_rep_get(hl_appl);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    return result;

}

/**
hl_multi_lvl_sup_cb - multi level switch type callback
@param[in]  ifd interface
@param[in]  pri_type    primary switch type, SW_TYPE_XX
@param[in]  sec_type    secondary switch type , SW_TYPE_XX.
@return
*/
void hl_multi_lvl_sup_cb(zwifd_p ifd,  uint8_t pri_type, uint8_t sec_type)
{
    ALOGI("Primary switch type:%u", pri_type);
    ALOGI("Secondary switch type:%u", sec_type);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Multi Level Switch Type Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    cJSON_AddNumberToObject(jsonRoot, "Primary Switch Type", pri_type);
    cJSON_AddNumberToObject(jsonRoot, "Secondary Switch Type", sec_type);

    if(resCallBack)
    {
        char *p = cJSON_Print(jsonRoot);

        if(p)
        {
            resCallBack(p);
            free(p);
        }
    }

    cJSON_Delete(jsonRoot);
}

/**
hl_multi_lvl_sup - Get switch type
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_multi_lvl_sup(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_level_sup_get(ifd, hl_multi_lvl_sup_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0 && result != 1)
    {
        ALOGE("zwif_level_sup_get with error:%d", result);
    }

    return result;
}

int zwcontrol_get_support_switch_type(hl_appl_ctx_t* hl_appl, int nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    ALOGI("zwcontroller_get_support_switch started");
    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_MULTILEVEL))
    {
        return -1;
    }

    int result = hl_multi_lvl_sup(hl_appl);
    if(result == 0 || result == 1)
    {
        result = 0;
        ALOGD("supported switch multilevel setup done.");
    }
    return result;
}

/**
hl_multi_lvl_set - Set multi-level switch level
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_multi_lvl_set(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_level_set(ifd, (uint8_t)hl_appl->mul_lvl_val, hl_appl->mul_lvl_dur);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_multi_lvl_set with error:%d", result);
    }

    return result;

}


int zwcontrol_switch_multilevel_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint16_t levelValue, uint8_t duration)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    ALOGI("zwcontrol_switch_level_set started");
    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_MULTILEVEL))
    {
        return -1;
    }
    hl_appl->mul_lvl_val = levelValue;
    hl_appl->mul_lvl_dur = duration;

    int result = hl_multi_lvl_set(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_switch_level_set with error:%d",result);
    }

    return result;
}

/**
hl_multi_lvl_chg - toggle between start and stop level change
@param[in]  hl_appl     The high-level api context
@return
*/
void    hl_multi_lvl_chg(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->temp_desc);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        ALOGE("hl_multi_lvl_chg: interface descriptor:%u not found", hl_appl->temp_desc);
        return ;
    }

    if (hl_appl->mul_lvl_change_started == 0)
    {
        zwlevel_t lvl_ctl;

        lvl_ctl.dur = hl_appl->mul_lvl_dur;
        lvl_ctl.pri_dir = hl_appl->mul_lvl_dir;
        lvl_ctl.pri_level = (uint8_t)hl_appl->mul_lvl_val;
        lvl_ctl.pri_ignore_lvl = (uint8_t)((hl_appl->mul_lvl_val == 0xFF)? 1:0);
        lvl_ctl.sec_dir = hl_appl->mul_lvl_sec_dir;
        lvl_ctl.sec_step = hl_appl->mul_lvl_sec_step;

        //Change state to start level change
        result = zwif_level_start(ifd, &lvl_ctl);

        plt_mtx_ulck(hl_appl->desc_cont_mtx);

        if (result != 0)
        {
            ALOGE("zwif_level_start with error:%d", result);
            return;
        }

        ALOGI("Start level change ...");
        hl_appl->mul_lvl_change_started = 1;
    }
    else
    {
        //Change state to start level change
        result = zwif_level_stop(ifd);

        plt_mtx_ulck(hl_appl->desc_cont_mtx);

        if (result != 0)
        {
            ALOGE("zwif_level_stop with error:%d", result);
            return;
        }

        ALOGI("Stop level change ...");
        hl_appl->mul_lvl_change_started = 0;
    }
}

int zwcontrol_start_stop_switchlevel_change(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint16_t startLvlVal, 
                                            uint8_t duration, uint8_t pmyChangeDir, uint8_t secChangeDir, uint8_t secStep)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_MULTILEVEL))
    {
        return -1;
    }

    if(hl_appl->mul_lvl_change_started == 1){
        ALOGI("level change already started, now stop level change");
        hl_multi_lvl_chg(hl_appl);
        return 0;
    }
    // zwcontrol_start_stop_switch_change(hl_appl, 1, 255, 30, 0, 0, 255);
    // Primary switch start level, enter 0 to 99 or 255 to use device current level
    hl_appl->mul_lvl_val = startLvlVal;
    // Dimming duration in seconds which is the interval it takes to dim from level 0 to 99
    hl_appl->mul_lvl_dur = duration;
    // destination desc id
    // hl_appl->temp_desc = 0;
    // Primary switch change dir: (0) Up, (1) Down, (3) No change (for version 3 switch)
    hl_appl->mul_lvl_dir = pmyChangeDir;
    // Secondary switch change dir: (0) Up, (1) Down, (3) No change
    hl_appl->mul_lvl_sec_dir = secChangeDir;
    // Secondary switch step size, enter 0 to 99 or 255 to use default value
    hl_appl->mul_lvl_sec_step = secStep;
    if(secChangeDir == 3)
    {
        hl_appl->mul_lvl_sec_step = 0;
    }

    ALOGD("startLvlVal:%d, duration:%d, pmyChangeDir:%d, secChangeDir:%d, secStep:%d",
           startLvlVal, duration, pmyChangeDir, secChangeDir, secStep);

    hl_multi_lvl_chg(hl_appl);

    return 0;
}

/**
hl_cfg_report_cb - Get configuration parameter callback
@param[in]  ifd     interface
@param[in]  param   parameter value
*/
void hl_cfg_report_cb(zwifd_p ifd, zwconfig_p param)
{
    int32_t         param_value;
    zwnetd_p        net_desc;
    hl_appl_ctx_t   *hl_appl;

    if (!hl_int_get(param->data, param->size, &param_value))
    {   //Error, default to zero
        param_value = 0;
    }
    ALOGI("Configuration parameter:%u, value:%d", param->param_num, param_value);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Configuration Get Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    cJSON_AddNumberToObject(jsonRoot, "Configuration parameter", param->param_num);
    cJSON_AddNumberToObject(jsonRoot, "Configuration value", param_value);

    if(resCallBack)
    {
        char *p = cJSON_Print(jsonRoot);

        if(p)
        {
            resCallBack(p);
            free(p);
        }
    }

    cJSON_Delete(jsonRoot);

    //Check whether to get another report
    net_desc = zwnet_get_desc(ifd->net);

    hl_appl = (hl_appl_ctx_t *)net_desc->user;

    if (hl_appl->cfg_param_mode == 1)
    {
        if (param->param_num < hl_appl->cfg_range_end)
        {
            zwif_config_get(ifd, param->param_num + 1);
        }
    }
}

/**
hl_cfg_rep_setup - Setup a configuration parameter report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_cfg_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);

    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_config_rpt_set(ifd, hl_cfg_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_cfg_rep_setup with error:%d", result);
    }

    return result;
}

/**
hl_cfg_get - Get configuration parameter
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_cfg_get(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p     ifd;
    uint8_t     param_num;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->temp_desc);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    //Check whether to get single or a range of parameter value
    if (hl_appl->cfg_param_mode == 0)
    {   //Single
        param_num = hl_appl->cfg_param;
    }
    else
    {
        //Multiple
        if (hl_appl->cfg_range_start > hl_appl->cfg_range_end)
        {
            hl_appl->cfg_range_end = hl_appl->cfg_range_start;
        }
        param_num = (uint8_t)hl_appl->cfg_range_start;

    }

    result = zwif_config_get(ifd, param_num);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_cfg_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_configuration_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t paramMode, uint8_t paramNumber,
                                 uint16_t rangeStart, uint16_t rangeEnd)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_CONFIGURATION))
    {
        return -1;
    }

    int result = hl_cfg_rep_setup(hl_appl);
    if(result != 0)
    {
        ALOGE("hl_cfg_rep_setup with error:%d",result);
        return result;
    }
    // How many parameters to get: (0) Single, (1) Range
    hl_appl->cfg_param_mode = paramMode;
    if (hl_appl->cfg_param_mode == 0)
    {
        ALOGI("configurations get, param:%d", paramNumber);
        // Parameter number
        hl_appl->cfg_param = paramNumber;
    }
    else
    {
        ALOGI("configurations get, param range %d to %d", rangeStart,rangeEnd);
        // Parameter range start
        hl_appl->cfg_range_start = rangeStart;
        // Parameter range end
        hl_appl->cfg_range_end = rangeEnd;
    }

    result = hl_cfg_get(hl_appl);
    if(result != 0)
    {
        ALOGE("hl_cfg_get with error:%d",result);
    }
    return result;
}

/**
hl_cfg_set - Set configuration parameter
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_cfg_set(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p     ifd;
    zwconfig_t  param;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->temp_desc);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    //Check for invalid size
    switch (hl_appl->cfg_size)
    {
        case 1:
            param.data[0] = (uint8_t)(hl_appl->cfg_value & 0xFF);
            break;

        case 2:
            param.data[0] = (uint8_t)((hl_appl->cfg_value >> 8) & 0xFF);
            param.data[1] = (uint8_t)(hl_appl->cfg_value & 0xFF);
            break;

        case 4:
            param.data[0] = (uint8_t)((hl_appl->cfg_value >> 24) & 0xFF);
            param.data[1] = (uint8_t)((hl_appl->cfg_value >> 16) & 0xFF);
            param.data[2] = (uint8_t)((hl_appl->cfg_value >> 8) & 0xFF);
            param.data[3] = (uint8_t)(hl_appl->cfg_value & 0xFF);
            break;

        default:
            ALOGW("Invalid config param size:%u", hl_appl->cfg_size);
            return ZW_ERR_VALUE;
    }

    param.param_num = hl_appl->cfg_param;
    param.size = hl_appl->cfg_size;
    param.use_default = hl_appl->cfg_value_default;

    result = zwif_config_set(ifd, &param);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_cfg_set with error:%d", result);
    }

    return result;
}

int zwcontrol_configuration_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t paramNumber,
                                uint8_t paramSize, uint8_t useDefault, int32_t paramValue)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_CONFIGURATION))
    {
        return -1;
    }
    // Parameter number
    hl_appl->cfg_param =  paramNumber;
    // Parameter value size (1, 2 or 4 bytes)
    hl_appl->cfg_size = paramSize;
    // Use default value
    hl_appl->cfg_value_default = useDefault;
    if (!hl_appl->cfg_value_default)
    {
        // Parameter value
        hl_appl->cfg_value = paramValue;
    }
    else
    {
        hl_appl->cfg_value = 0;
    }
    ALOGI("configuration set, param:%d, useDefault:%d, value:%d",paramNumber,useDefault,hl_appl->cfg_value);

    int result = hl_cfg_set(hl_appl);
    if (result != 0)
    {
        ALOGE("hl_cfg_set with error:%d", result);
    }
    return result;
}

/**
hl_power_level_report_cb - power level command report callback
@param[in]  ifd     The interface that received the report
@param[in]  bylvl   Power level indicator value
@param[in]  bytimeout   Time out value
@return
*/
void hl_power_level_report_cb(zwifd_p ifd, uint8_t bylvl, uint8_t bytimeout)
{
    ALOGI("Power level is %s", power_level_ind_str[bylvl]);
    if (bylvl != POWERLEVEL_REPORT_NORMALPOWER)
    {
        ALOGW("Time out is %u seconds.", bytimeout);
    }

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Power Level Get Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    cJSON_AddStringToObject(jsonRoot, "Power Level", power_level_ind_str[bylvl]);

    if(resCallBack)
    {
        char *p = cJSON_Print(jsonRoot);

        if(p)
        {
            resCallBack(p);
            free(p);
        }
    }

    cJSON_Delete(jsonRoot);
}

/**
hl_power_level_rep_setup - Setup power level command report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_power_level_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_power_level_rpt_set(ifd, hl_power_level_report_cb);
    //result2 = zwif_power_level_test_rpt_set(ifd, hl_power_level_test_node_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_power_level_rep_setup with error:%d for set up power level report",
                        result);
    }

    /*if (result2 != 0)
    {
        ALOGE("hl_power_level_rep_setup with error:%d for set up power level test report",
                        result2);
    }*/

    return result;
}

/**
hl_power_level_rep_get - Get the power level
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_power_level_rep_get(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_power_level_get(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result == 1)
    {
        ALOGE("hl_power_level_rep_get command queued.");
    }
    else if (result != 0)
    {
        ALOGE("hl_power_level_rep_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_powerLevel_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_POWERLEVEL))
    {
        return -1;
    }

    int result = hl_power_level_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGD("hl_power_level_rep_setup done.");
        result = hl_power_level_rep_get(hl_appl);
    }

    return result;
}

/**
hl_basic_set - basic command set value
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_swich_all_on(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_switch_all_on(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGW("hl_swich_all_on with error:%d", result);
    }

    return result;

}

int  zwcontrol_swith_all_on(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_ALL))
    {
        return -1;
    }
    int result = hl_swich_all_on(hl_appl);
    if(result != 0)
    {
        ALOGW("hl_swich_all_on with error:%d",result);
    }
    return result;
}

/**
hl_swich_all_off - set switch all off
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_swich_all_off(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_switch_all_off(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGW("hl_swich_all_off with error:%d", result);
    }

    return result;

}

int  zwcontrol_swith_all_off(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_ALL))
    {
        return -1;
    }
    int result = hl_swich_all_off(hl_appl);
    if(result != 0)
    {
        ALOGW("hl_swich_all_off with error:%d",result);
    }
    return result;
}

/**
hl_switch_all_set - switch all command set value
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_switch_all_set(hl_appl_ctx_t   *hl_appl, uint8_t value)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_switch_all_set(ifd, (uint8_t)value);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGW("hl_switch_all_set with error:%d", result);
    }

    return result;
}

int  zwcontrol_swith_all_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t value)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_ALL))
    {
        return -1;
    }
    int result = hl_switch_all_set(hl_appl, (uint8_t)value);
    if(result != 0)
    {
        ALOGW("hl_swich_all_set with error:%d",result);
    }
    return result;
}

/**
hl_switch_all_get_report_cb - switch all command report callback
@param[in]  ifd     The interface that received the report
@param[in]  mode   Switch all indicator value
@return
*/
void hl_switch_all_get_report_cb(zwifd_p ifd, uint8_t mode)
{
    ALOGI("Switch all mode is %d", mode);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Switch All Get Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    cJSON_AddNumberToObject(jsonRoot, "mode", mode);

    if(resCallBack)
    {
        char *p = cJSON_Print(jsonRoot);

        if(p)
        {
            resCallBack(p);
            free(p);
        }
    }

    cJSON_Delete(jsonRoot);
}

/**
hl_swith_all_get_rep_setup - Setup switch all get command report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_swith_all_get_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_switch_all_get_rpt_set(ifd, hl_switch_all_get_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_swith_all_get_rep_setup with error:%d for set up switch all get report",
                        result);
    }

    return result;
}

/**
hl_switch_all_rep_get - Get the switch all
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_switch_all_rep_get(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_switch_all_get(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result == 1)
    {
        ALOGE("zwif_switch_all_get command queued.");
    }
    else if (result != 0)
    {
        ALOGE("zwif_switch_all_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_swith_all_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_ALL))
    {
        return -1;
    }
    int result = hl_swith_all_get_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGW("hl_swich_all_set done");
        result = hl_switch_all_rep_get(hl_appl);
    }
    return result;
}