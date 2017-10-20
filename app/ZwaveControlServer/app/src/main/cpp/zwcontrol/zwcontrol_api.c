#include <stdio.h>
#include <unistd.h>
#include "zwcontrol_api.h"
#include "zw_sec2_wrap.h"

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

            case COMMAND_CLASS_SECURITY_2:
            {
                return "COMMAND_CLASS_SECURITY_2";
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
            /******************skysoft******************/
            case COMMAND_CLASS_SWITCH_COLOR:
            {
                return "COMMAND_CLASS_SWITCH_COLOR";
            }
            break;

            case COMMAND_CLASS_BASIC_TARIFF_INFO:
            {
                return "COMMAND_CLASS_BASIC_TARIFF_INFO";
            }
            break;

            case COMMAND_CLASS_BARRIER_OPERATOR:
            {
                return "COMMAND_CLASS_BARRIER_OPERATOR";
            }
            break;

            case COMMAND_CLASS_LANGUAGE:
            {
                return "COMMAND_CLASS_LANGUAGE";
            }
            break;

            case COMMAND_CLASS_CENTRAL_SCENE:
            {
                return "COMMAND_CLASS_CENTRAL_SCENE";
            }
            break;

            /******************skysoft******************/
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
hl_lrn_mod_set - Start learn mode
@param[in]	hl_appl		The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_lrn_mod_set(hl_appl_ctx_t   *hl_appl)
{
    int result;

    result = zwnet_initiate(hl_appl->zwnet);

    if (result != 0)
    {
        plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_lrn_mod_set with error:%d", result);
    }

    return result;
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

    sec2_destroy();

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
#ifdef USING_SERIAL_DIR
    unsigned    port_num = 2;
    sprintf(hl_appl->comm_port_name, "/dev/ttyS%d", port_num);
    ALOGI("Controller using serial port: %s",hl_appl->comm_port_name);
    zw_init.comm_port_name = hl_appl->comm_port_name;
#endif
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

    sec2_init(zw_init_ret.net_id);

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

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Sensor Info Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddStringToObject(jsonRoot, "type", sensor_type_str[value->type]);
    cJSON_AddNumberToObject(jsonRoot, "precision", value->precision);
    cJSON_AddStringToObject(jsonRoot, "unit", *unit_str);

    if (!hl_int_get(value->data, value->size, &snsr_value))
    {   //Error, default to zero
        snsr_value = 0;
    }

    char float_str[80] = {0};

    if (value->precision == 0)
    {
        ALOGD("Sensor reading:%d", snsr_value);
        cJSON_AddNumberToObject(jsonRoot, "value", snsr_value);
    }
    else
    {
        hl_float_get(snsr_value, value->precision, 80, float_str);
        ALOGD("Sensor reading:%s", float_str);
        cJSON_AddStringToObject(jsonRoot, "value", float_str);
    }

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

// Command Class Basic

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
hl_basic_report_v2_cb - basic command report version 2 callback
*/
void hl_basic_report_v2_cb(zwifd_p ifd, uint8_t cur_val, uint8_t tar_val, uint8_t duration)
{
    ALOGI("Basic command version 2 report, current value is %02Xh, target value is %02x, duration is %02x",
          cur_val, tar_val, duration);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Basic get v2 Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    char str[50] = {0};
    sprintf(str, "%02Xh", cur_val);
    cJSON_AddStringToObject(jsonRoot, "current value", str);

    char str1[50] = {0};
    sprintf(str1, "%02Xh", tar_val);
    cJSON_AddStringToObject(jsonRoot, "target value", str1);

    char str2[50] = {0};
    sprintf(str2, "%02Xh", duration);
    cJSON_AddStringToObject(jsonRoot, "duration", str2);

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

    if(ifd->ver == 2)
    {
        result = zwif_basic_rpt_set_v2(ifd, hl_basic_report_v2_cb);
    }
    else
    {
        result = zwif_basic_rpt_set(ifd, hl_basic_report_cb);
    }

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

// Command Class Switch Multi-level 

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
hl_swml_v4_report_cb - Switch multi-level report v4 callback
@param[in]  ifd      interface
@param[in]  cur_val  current value
@param[in]  tar_val  target value
@param[in]  duration duration
*/
void hl_swml_v4_report_cb(zwifd_p ifd, uint8_t cur_val, uint8_t tar_val, uint8_t duration)
{
    ALOGI("Switch multi-level report, current value:%02x, target value:%02x, duration:%02x", cur_val, tar_val, duration);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Switch Multi-level V4 Report Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    char str0[50] = {0};
    char str1[50] = {0};
    char str2[50] = {0};
    sprintf(str0, "%02Xh", cur_val);
    sprintf(str1, "%02Xh", tar_val);
    sprintf(str2, "%02Xh", duration);

    cJSON_AddStringToObject(jsonRoot, "current value", str0);
    cJSON_AddStringToObject(jsonRoot, "target value", str1);
    cJSON_AddStringToObject(jsonRoot, "duration", str2);

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

    if(ifd->ver == 4){
        result = zwif_level_rpt_set_v4(ifd, hl_swml_v4_report_cb);
    }
    else 
    {
        result = zwif_level_rpt_set(ifd, hl_generic_report_cb);
    }

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

// Command Class Configuration

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
    ALOGI("Configuration parameter:%u, value:%x", param->param_num, param_value);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Configuration Get Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    cJSON_AddNumberToObject(jsonRoot, "Parameter number", param->param_num);
    char str[50] = {0};
    sprintf(str, "%x", param_value);
    cJSON_AddStringToObject(jsonRoot, "Parameter value", str);

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

int32_t hl_cfg_bulk_set(hl_appl_ctx_t* hl_appl, uint8_t offset1, uint8_t offset2, uint8_t paramNumber, uint32_t* paramValue)
{
    int         result;
    zwifd_p     ifd;
    zwconfig_bulk_t  param;
    int i;

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
            for (i = 0; i < paramNumber; i++)
            {
                param.data[i][0] = (uint8_t)(paramValue[i] & 0xFF);
            }
            break;

        case 2:
            for (i = 0; i < paramNumber; i++)
            {
                param.data[i][0] = (uint8_t)((paramValue[i] >> 8) & 0xFF);
                param.data[i][1] = (uint8_t)(paramValue[i] & 0xFF);
            }

            break;

        case 4:
            for (i = 0; i < paramNumber; i++)
            {
                param.data[i][0] = (uint8_t)((paramValue[i] >> 24) & 0xFF);
                param.data[i][1] = (uint8_t)((paramValue[i] >> 16) & 0xFF);
                param.data[i][2] = (uint8_t)((paramValue[i] >> 8) & 0xFF);
                param.data[i][3] = (uint8_t)(paramValue[i] & 0xFF);
            }
            break;

        default:
            ALOGW("hl_cfg_bulk_set, Invalid config param size:%u", hl_appl->cfg_size);
            return ZW_ERR_VALUE;
    }

    param.offset1 = offset1;
    param.offset2 = offset2;
    param.num_of_param = paramNumber;
    param.param_size = hl_appl->cfg_size;
    param.use_default = hl_appl->cfg_value_default;

    result = zwif_config_set_v2(ifd, &param);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_config_set_v2 with error:%d", result);
    }

    return result;
}

// ver 2~4
int  zwcontrol_configuration_bulk_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t offset1, uint8_t offset2,
                                      uint8_t paramNumber, uint8_t paramSize, uint8_t useDefault, uint32_t* paramValue)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_CONFIGURATION))
    {
        return -1;
    }

    hl_appl->cfg_value_default = useDefault;
    hl_appl->cfg_size = paramSize;
    if (hl_appl->cfg_value_default)
    {
        *paramValue = 0;
    }

    int result = hl_cfg_bulk_set(hl_appl, offset1, offset2, paramNumber, &paramValue);
    if (result != 0)
    {
        ALOGE("hl_cfg_bulk_set with error:%d", result);
    }
    return result;
}

// Command Class Power Level

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

// Command Class Switch All

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

int zwcontrol_start_learn_mode(hl_appl_ctx_t* hl_appl)
{
    return hl_lrn_mod_set(hl_appl);
}

// Command Class Switch Binary

/**
hl_bin_set - Turn binary switch on/off
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_bin_set(hl_appl_ctx_t   *hl_appl, uint8_t duration)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->temp_desc);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_switch_set(ifd, hl_appl->bin_state, duration);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_switch_set with error:%d", result);
    }

    return result;

}

int zwcontrol_switch_binary_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t bin_state, uint8_t duration)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_BINARY))
    {
        return -1;
    }

    hl_appl->bin_state = bin_state;
    int result = hl_bin_set(hl_appl, (uint8_t)duration);
    if(result != 0)
    {
        ALOGW("hl_bin_set with error:%d",result);
    }
    return result;
}

/**
hl_bin_report_cb - binary switch report callback
@param[in]  ifd The interface that received the report
@param[in]  on          0=off, else on
@return
*/
void hl_bin_report_cb(zwifd_p ifd, uint8_t on)
{
    ALOGI("Binary switch state is %s", (on)? "on" : "off");

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Binary Switch Get Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    if(on)
    {
        cJSON_AddStringToObject(jsonRoot, "state", "on");  
    } 
    else
        cJSON_AddStringToObject(jsonRoot, "state", "off");

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

// for switch binary version 2
void hl_bin_v2_report_cb(zwifd_p ifd, uint8_t on, uint8_t duration)
{
    ALOGI("Binary switch version 2 state is %s, duration is %02x", (on)? "on" : "off", duration);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Binary Switch Get ver2 Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    if(on)
    {
        cJSON_AddStringToObject(jsonRoot, "state", "on");  
    } 
    else
        cJSON_AddStringToObject(jsonRoot, "state", "off");

    cJSON_AddNumberToObject(jsonRoot, "duration", duration);

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
hl_bin_rep_setup - Setup binary switch report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_bin_rep_setup(hl_appl_ctx_t   *hl_appl)
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

    if (ifd->ver == 2)
    {
        result = zwif_switch_rpt_set_v2(ifd, hl_bin_v2_report_cb);
    }
    else
    {
        result = zwif_switch_rpt_set(ifd, hl_bin_report_cb);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_bin_rep_setup with error:%d", result);
    }

    return result;
}

/**
hl_binary_rep_get - Get binary switch state
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_binary_rep_get(hl_appl_ctx_t   *hl_appl)
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
        result = zwif_switch_get_poll(ifd, &hl_appl->poll_req);
        if (result == 0)
        {
            ALOGI("Polling request handle:%u", hl_appl->poll_req.handle);
        }
    }
    else
    {
        result = zwif_switch_get(ifd);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_binary_rep_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_switch_binary_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_BINARY))
    {
        return -1;
    }

    int result = hl_bin_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_bin_rep_setup done");
    }

    result = hl_binary_rep_get(hl_appl);
    if(result != 0){
        ALOGE("zwcontrol_switch_binary_get with error:%d",result);
    }

    return result;
}

// Command Class Sensor Binary v2

 const static char *binary_sensor_type[]=
 {
    "unknown", "General purpose", "Smoke","CO",
    "CO2", "Heat", "Water", "freeze", 
    "Tamper", "Aux", "Door/Window", "Tilt",
    "Motion", "Glass break"
};

/**
hl_bin_snsr_rep_cb - binary sensor report callback
@param[in]  ifd The interface that received the report
@param[in]  state       The state of the sensor: 0=idle, else event detected
@return
*/
void hl_bin_snsr_rep_cb(zwifd_p ifd, uint8_t value, uint8_t type)
{
    ALOGI("Binary sensor value :%s, sensor type :%d ", (value == 0)? "idle" : "event detected",type);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Binary Sensor Get Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddStringToObject(jsonRoot, "Event Type", binary_sensor_type[type]);
    if(value == 0)
    {
        cJSON_AddStringToObject(jsonRoot, "state", "idle");  
    } 
    else
        cJSON_AddStringToObject(jsonRoot, "state", "event detected");

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
hl_bin_snsr_rep_setup - Setup binary sensor report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_bin_snsr_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_bsensor_rpt_set(ifd, hl_bin_snsr_rep_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_bin_snsr_rep_setup with error:%d", result);
    }

    return result;
}

/**
hl_bin_snsr_rep_get - Get binary sensor state report
@param[in]  hl_appl     The high-level api context
@param[in]  sensor_type The sensor type want to get
@return  0 on success, negative error number on failure
*/
int32_t hl_bin_snsr_rep_get(hl_appl_ctx_t   *hl_appl, uint8_t sensor_type)
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
        result = zwif_bsensor_get_poll(ifd, sensor_type, &hl_appl->poll_req);
        if (result == 0)
        {
            ALOGI("Polling request handle:%u", hl_appl->poll_req.handle);
        }
    }
    else
    {
        result = zwif_bsensor_get(ifd, (uint8_t)sensor_type);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGI("hl_bin_snsr_rep_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_sensor_binary_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t sensor_type)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SENSOR_BINARY_V2))
    {
        return -1;
    }

    int result = hl_bin_snsr_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_bin_snsr_rep_setup done");
    }

    result = hl_bin_snsr_rep_get(hl_appl, sensor_type);
    if(result != 0)
    {
        ALOGE("zwcontrol_sensor_binary_get with error: %d", result);
    }

    return result;
}

/**
hl_bin_snsr_sup_rep_cb - binary sensor support report callback
@param[in]  ifd The interface that received the report
@param[in]  
@return
*/
void hl_bin_snsr_sup_rep_cb(zwifd_p ifd, uint8_t type_len, uint8_t *type)
{
    ALOGI("Binary sensor supported number: %d",type_len);        

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Binary Sensor Support Get Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    for(int i = 0; i < type_len; i++)
    {
        ALOGI("Supported binary sensor type is :%s",binary_sensor_type[type[i]]);
        cJSON_AddStringToObject(jsonRoot, "Supported type", binary_sensor_type[type[i]]);
    }

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
hl_bin_snsr_sup_rep_setup - Setup binary sensor support report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_bin_snsr_sup_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_bsensor_sup_rpt_set(ifd, hl_bin_snsr_sup_rep_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_bin_snsr_rep_setup with error:%d", result);
    }

    return result;
}

/**
hl_bin_snsr_sup_rep_get - Get binary sensor support report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_bin_snsr_sup_rep_get(hl_appl_ctx_t   *hl_appl)
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
        result = zwif_bsensor_sup_get_poll(ifd, &hl_appl->poll_req);
        if (result == 0)
        {
            ALOGI("Polling request handle:%u", hl_appl->poll_req.handle);
        }
    }
    else
    {
        result = zwif_bsensor_sup_get(ifd);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGI("hl_bin_snsr_rep_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_sensor_binary_supported_sensor_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SENSOR_BINARY_V2))
    {
        return -1;
    }

    int result = hl_bin_snsr_sup_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_bin_snsr_sup_rep_setup done");
    }

    result = hl_bin_snsr_sup_rep_get(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_sensor_binary_support_sensor_get with error: %d", result);
    }

    return result;
}

// Command Class Meter v3

static const char *meter_type[] = { "unknown", "electric", "gas", "water",/*ver 2-4*/
                                    "heating", "cooling"/*ver 5*/};
static const char *meter_rate[] = { "unknown", "import(consumed)", "export(produced)"};

static const char units[3][7] = {
    {"KWh", "kVAh", "W", "Pulse Count", "V", "A", "Power factor"},
    {"Cubic meters", "Cubic feet", "unknown", "Pulse Count", "unknown", "unknown", "unknown"},
    {"Cubic meters", "Cubic feet", "US gallons", "Pulse Count", "unknown", "unknown", "unknown"}
};

//static const char *ele_unit[] = {"KWh", "kVAh", "W", "Pulse Count", "V", "A", "Power factor"};

//static const char *gas_unit[] = {"Cubic meters", "Cubic feet", "unknown", "Pulse Count"};

//static const char *water_unit[] = {"Cubic meters", "Cubic feet", "US gallons", "Pulse Count"};

/**
hl_meter_rep_cb - meter report callback
@param[in]  ifd     The interface that received the report
@param[in]  data    current value and unit of the meter
@return
*/
static void hl_meter_rep_cb(zwifd_p ifd, zwmeter_dat_p value)
{
    int32_t meter_value;

    ALOGI("Meter type:%s, precision:%u, unit:%s, rate type:%s",
                 meter_type[value->type], value->precision, units[value->type+1][value->unit],
                 meter_rate[value->rate_type]);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Meter report Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddStringToObject(jsonRoot, "Meter type", meter_type[value->type]);
    cJSON_AddStringToObject(jsonRoot, "Rate type", meter_rate[value->rate_type]);

    if (!hl_int_get(value->data, value->size, &meter_value))
    {   //Error, default to zero
        meter_value = 0;
    }

    if (value->precision == 0)
    {
        ALOGI("Meter reading:%d", meter_value);
        cJSON_AddNumberToObject(jsonRoot, "Meter reading", meter_value);
    }
    else
    {
        char    float_str[80];
        hl_float_get(meter_value, value->precision, 80, float_str);
        ALOGI("Meter reading:%s", float_str);
        cJSON_AddStringToObject(jsonRoot, "Meter reading", float_str);
    }

    //Check if to display previous reading, condition: delta time > 0
    if (value->delta_time > 0)
    {
        if (!hl_int_get(value->prv_data, value->size, &meter_value))
        {   //Error, default to zero
            meter_value = 0;
        }

        if (value->precision == 0)
        {
            ALOGI("Previous Meter reading:%d, taken %us ago", meter_value, value->delta_time);
            cJSON_AddNumberToObject(jsonRoot, "Previous meter reading", meter_value);
            cJSON_AddNumberToObject(jsonRoot, "Taken time(sec)", value->delta_time);
        }
        else
        {
            char    float_str[80];
            hl_float_get(meter_value, value->precision, 80, float_str);
            ALOGI("Previous Meter reading:%s, taken %us ago", float_str, value->delta_time);
            cJSON_AddStringToObject(jsonRoot, "Previous meter reading", float_str);
            cJSON_AddNumberToObject(jsonRoot, "Taken time(sec)", value->delta_time);
        }
    }
    cJSON_AddStringToObject(jsonRoot, "Meter unit", units[value->type+1][value->unit]);

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
hl_meter_rep_setup - Setup meter report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_meter_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_meter_rpt_set(ifd, hl_meter_rep_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_meter_rep_setup with error:%d", result);
    }

    return result;
}

/**
hl_meter_rep_get - Get meter report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_meter_rep_get(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_meter_get(ifd, hl_appl->meter_unit);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result < 0)
    {
        ALOGE("hl_meter_rep_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_meter_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t meter_unit)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_METER_V3))
    {
        return -1;
    }

    int result = hl_meter_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_meter_rep_setup done");
    }

    //puts("Preferred unit:");
    //puts("For electric meter: (0) kWh (1) kVAh (2) W (3) Pulse count");
    //puts("For gas meter: (0) cubic meter (1) cubic feet  (3) Pulse count");
    //puts("For water meter: (0) cubic meter (1) cubic feet (2) US gallons (3) Pulse count");
    hl_appl->meter_unit = meter_unit;
    result = hl_meter_rep_get(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_meter_get with error:%d",result);
    }

    return result;
}

/**
hl_meter_sup_cb - report callback for meter capabilities
@param[in]  ifd         interface
@param[in]  meter_cap   meter capabilities
*/
void hl_meter_sup_cb(zwifd_p ifd, zwmeter_cap_p meter_cap)
{
    ALOGI("meter type:%s, meter supported unit bit-mask:%02x", meter_type[meter_cap->type], meter_cap->unit_sup);
    ALOGI("meter %s be reset", (meter_cap->reset_cap)? "can" : "can not");

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Meter Cap Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddStringToObject(jsonRoot, "Meter type", meter_type[meter_cap->type]);
    cJSON_AddStringToObject(jsonRoot, "Can be reset?", (meter_cap->reset_cap)? "can" : "can not");

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
hl_meter_sup - get information on the meter capabilities
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_meter_sup(hl_appl_ctx_t   *hl_appl)

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

    result = zwif_meter_sup_get(ifd, hl_meter_sup_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_group_cmd_sup_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_meter_supported_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_METER_V3))
    {
        return -1;
    }

    int result = hl_meter_sup(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_meter_supported_get with error: %d",result);
    }

    return result;
}

/**
hl_meter_reset - Reset all accumulated values stored in the meter device
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_meter_reset(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_meter_reset(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result < 0)
    {
        ALOGE("hl_meter_reset with error:%d", result);
    }

    return result;
}

int  zwcontrol_meter_reset(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_METER_V3))
    {
        return -1;
    }

    int result = hl_meter_reset(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_meter_reset with error:%d",result);
    }

    return result;
}

// Command Class Wake Up

/**
hl_wkup_rep_cb - wake up notification callback
@param[in]  ifd interface
@param[in]  cap capabilities report, null for notification
@return Only apply to notification: 0=no command pending to send; 1=commands pending to send.
*/
int hl_wkup_rep_cb(zwifd_p ifd, zwif_wakeup_p cap)
{
    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return 0;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Wake Up Report");

    if (!cap)
    {   //Notification
        ALOGI("Wake up notification from node:%u", ifd->nodeid);
        cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
        //Nothing to send, tell the node to sleep again
        //return 0;

        //Assume user has something to send
        //return 1;
    }
    else
    {   //Capabilities report
        ALOGI("Wake up settings:");
        cJSON *Wakeup_Info = cJSON_CreateObject();
        if(Wakeup_Info == NULL)
        {
            return 0;
        }

        cJSON_AddItemToObject(jsonRoot, "Wake up settings", Wakeup_Info);
        ALOGI("Alert receiving node: %u", cap->node.nodeid);
        ALOGI("Current interval: %u s", cap->cur);
        cJSON_AddNumberToObject(Wakeup_Info, "Alert receiving node", cap->node.nodeid);
        cJSON_AddNumberToObject(Wakeup_Info, "Current interval", cap->cur);
        if (cap->min == 0)
        {
            return 0;
        }
        ALOGI("Min: %u s, Max: %u s", cap->min, cap->max);
        ALOGI("Default: %u s, Step: %u s", cap->def, cap->interval);
        cJSON_AddNumberToObject(Wakeup_Info, "Min", cap->min);
        cJSON_AddNumberToObject(Wakeup_Info, "Max", cap->max);
        cJSON_AddNumberToObject(Wakeup_Info, "Default", cap->def);
        cJSON_AddNumberToObject(Wakeup_Info, "Step", cap->interval);
    }

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

    return 0;
}

/**
hl_wkup_get - Get wake up setting
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_wkup_get(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_wakeup_get(ifd, hl_wkup_rep_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_wkup_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_wake_up_interval_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_WAKE_UP))
    {
        return -1;
    }

    int result = hl_wkup_get(hl_appl);
    if(result != 0)
    {
        ALOGI("zwcontrol_wake_up_interval_get with error: %d",result);
    }

    return result;
}

/**
hl_wkup_set - Set wake up interval and alert receiving node
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_wkup_set(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p     ifd;
    zwnoded_p   noded;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->temp_desc);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    noded = hl_node_desc_get(hl_appl->desc_cont_hd, hl_appl->node_desc_id);
    if (!noded && hl_appl->node_desc_id)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_NODE_NOT_FOUND;
    }

    result = zwif_wakeup_set(ifd, hl_appl->wkup_interval, noded);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_wkup_set with error:%d", result);
    }

    return result;
}

int  zwcontrol_wake_up_interval_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint32_t wkup_interval)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_WAKE_UP))
    {
        return -1;
    }

    hl_appl->node_desc_id = 1; // controller nodeid
    hl_appl->wkup_interval = wkup_interval;

    int result = hl_wkup_set(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_wake_up_interval_set with error: %d",result);
    }

    return result;
}

// Command Class Door Lock 

/**
hl_dlck_op_report_cb - Door lock operation status report callback
@param[in]  ifd     The interface that received the report
@param[in]  op_sts  Operation status
@return
*/
void hl_dlck_op_report_cb(zwifd_p ifd, zwdlck_op_p  op_sts)
{
    ALOGI("Door lock operation mode:%02X,", op_sts->mode);
    ALOGI("Outside door handles mode:%02X,", op_sts->out_mode);
    ALOGI("Inside door handles mode:%02X,", op_sts->in_mode);
    ALOGI("Door condition:%02X,", op_sts->cond);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Door Lock Operation Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddNumberToObject(jsonRoot, "Door Lock op mode", op_sts->mode);
    cJSON_AddNumberToObject(jsonRoot, "Outside Door mode", op_sts->out_mode);
    cJSON_AddNumberToObject(jsonRoot, "Inside Door mode", op_sts->in_mode);
    cJSON_AddNumberToObject(jsonRoot, "Door Condition", op_sts->cond);

    if (op_sts->tmout_min != 0xFE)
    {
        ALOGI("Remaining time in unsecured state:%u:%u,",
                     op_sts->tmout_min, op_sts->tmout_sec);
        cJSON_AddNumberToObject(jsonRoot, "Unsecured State Time(min)", op_sts->tmout_min);
        cJSON_AddNumberToObject(jsonRoot, "Unsecured State Time(sec)", op_sts->tmout_sec);
    }


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
hl_dlck_op_rep_setup - Setup door lock operation status report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_dlck_op_rep_setup(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_dlck_op_rpt_set(ifd, hl_dlck_op_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGI("hl_dlck_op_rep_setup with error:%d", result);
    }

    return result;
}

/**
hl_dlck_op_rep_get - Get the state of the door lock device
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_dlck_op_rep_get(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_dlck_op_get(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGI("hl_dlck_op_rep_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_door_lock_operation_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_DOOR_LOCK))
    {
        return -1;
    }

    int result = hl_dlck_op_rep_setup(hl_appl);
    if(result == 0){
        ALOGI("hl_dlck_op_rep_setup done.");
        result = hl_dlck_op_rep_get(hl_appl);
    }

    return result;
}

/**
hl_dlck_op_set - Set door lock operation
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_dlck_op_set(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_dlck_op_set(ifd, hl_appl->dlck_mode);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGI("hl_dlck_op_set with error:%d", result);
    }

    return result;
}

int  zwcontrol_door_lock_operation_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t mode)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_DOOR_LOCK))
    {
        return -1;
    }

    // mode (hex)
    // (0) Door Unsecured, (1) Door Unsecured with timeout
    // (10) Door Unsecured for inside Door Handles, 16
    // (11) Door Unsecured for inside Door Handles with timeout, 17
    // (20) Door Unsecured for outside Door Handles, 32
    // (21) Door Unsecured for outside Door Handles with timeout, 33
    // (FE) Door/Lock State Unknown
    // (FF) Door Secured
    hl_appl->dlck_mode = mode;

    int result = hl_dlck_op_set(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_door_lock_operation_set with error: %d",result);
    }

    return result;
}

/**
hl_dlck_cfg_report_cb - Report callback for door lock configuration
@param[in]  ifd     interface
@param[in]  config  configuration
*/
void hl_dlck_cfg_report_cb(zwifd_p ifd, zwdlck_cfg_p  config)
{
    ALOGI("Door lock operation type:%s,",
                 (config->type == ZW_DOOR_OP_CONST)? "constant" : "timed");
    ALOGI("Outside door handles state:%02X,", config->out_sta);
    ALOGI("Inside door handles state:%02X,", config->in_sta);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Door Lock Configuration Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddStringToObject(jsonRoot, "Door Lock op type", (config->type == ZW_DOOR_OP_CONST)? "constant" : "timed");
    cJSON_AddNumberToObject(jsonRoot, "Outside Door state", config->out_sta);
    cJSON_AddNumberToObject(jsonRoot, "Inside Door state", config->in_sta);

    if (config->type == ZW_DOOR_OP_TIMED)
    {
        ALOGI("Time the lock stays unsecured.:%u:%u,",
                     config->tmout_min, config->tmout_sec);
        cJSON_AddNumberToObject(jsonRoot, "Unsecured State Time(min)", config->tmout_min);
        cJSON_AddNumberToObject(jsonRoot, "Unsecured State Time(sec)", config->tmout_sec);
    }

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
hl_dlck_cfg_get - Get configuration parameter
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_dlck_cfg_get(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_dlck_cfg_get(ifd, hl_dlck_cfg_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGI("hl_dlck_cfg_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_door_lock_config_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_DOOR_LOCK))
    {
        return -1;
    }

    int result = hl_dlck_cfg_get(hl_appl);
    if(result != 0)
    {
        ALOGI("zwcontrol_door_lock_config_get with error: %d",result);
    }

    return result;
}

/**
hl_dlck_cfg_set - Set the configuration of the door lock device
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_dlck_cfg_set(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_dlck_cfg_set(ifd, &hl_appl->dlck_config);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_dlck_cfg_set with error:%d", result);
    }

    return result;

}

int  zwcontrol_door_lock_config_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t type, uint8_t out_sta,
                                    uint8_t in_sta, uint8_t tmout_min, uint8_t tmout_sec)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_DOOR_LOCK))
    {
        return -1;
    }

    // "Operation type: (1)Constant (2)Timed
    hl_appl->dlck_config.type = type;
    // For door handles states, each bit represents a handle with bit set to 0 for disable; 1 for enable
    // Outside Door Handles State 0 to f (hex)
    hl_appl->dlck_config.out_sta = out_sta;
    // Inside Door Handles State 0 to f (hex)
    hl_appl->dlck_config.in_sta = in_sta;
    if (hl_appl->dlck_config.type == ZW_DOOR_OP_TIMED)
    {
        // Duration lock stays unsecured in
        // minutes (1-254)
        hl_appl->dlck_config.tmout_min = tmout_min;
        // seconds (1-59)
        hl_appl->dlck_config.tmout_sec = tmout_sec;
    }

    int result = hl_dlck_cfg_set(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_door_lock_config_set with error:%d",result);
    }

    return result;
}

// Command Class User Code

static const char *usrid_sts[] =
{
    "Available (not set)",
    "Occupied",
    "Reserved by administrator",
    "Status unavailable"
};

/**
hl_usrcod_report_cb - Report callback for user code
@param[in]  ifd         interface
@param[in]  usr_cod     user code and its status
*/
static void hl_usrcod_report_cb(zwifd_p ifd, zwusrcod_p  usr_cod)
{
    char usr_code[MAX_USRCOD_LENGTH + 1];

    if (usr_cod->id_sts > ZW_USRCOD_RSVD)
    {
        usr_cod->id_sts = 3;
    }

    memcpy(usr_code, usr_cod->u_code, usr_cod->code_len);
    /*int i = 0;
    for(i=0; i< usr_cod->code_len;i++)
        sprintf(usr_code[i], "%d", usr_cod->u_code[i]);*/
    usr_code[usr_cod->code_len] = '\0';

    ALOGI("User id:%u, status:%s, code:%s", usr_cod->id,
                 usrid_sts[usr_cod->id_sts], usr_code);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "User Code Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddNumberToObject(jsonRoot, "User ID", usr_cod->id);
    cJSON_AddStringToObject(jsonRoot, "Status", usrid_sts[usr_cod->id_sts]);
    cJSON_AddStringToObject(jsonRoot, "Code", usr_code);

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
hl_usrcod_get - Get user code
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_usrcod_get(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_usrcod_get(ifd, hl_appl->usr_id, hl_usrcod_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_usrcod_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_user_code_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t user_id)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_USER_CODE))
    {
        return -1;
    }

    // User ID (starting from 1)
    hl_appl->usr_id = user_id; 
    int result = hl_usrcod_get(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_user_code_get with error:%d",result);
    }

    return result;
}

/**
hl_usrcod_set - Set user code
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_usrcod_set(hl_appl_ctx_t *hl_appl)
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

    result = zwif_usrcod_set(ifd, &hl_appl->usr_code);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_usrcod_set with error:%d", result);
    }

    return result;
}

int  zwcontrol_user_code_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t user_id, uint8_t status)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_USER_CODE))
    {
        return -1;
    }

    // FIX ME, input user code by userself
    char user_code[MAX_USRCOD_LENGTH + 1] = {0xFF,0xFF,0xFF,0xFF};
    int  user_code_len;
    // User ID (0 for all users; starting from 1 for other user)
    hl_appl->usr_code.id = user_id;
    // prompt_str("User code:", MAX_USRCOD_LENGTH + 1, user_code);
    user_code_len = strlen(user_code);
    memcpy(hl_appl->usr_code.u_code, user_code, user_code_len);
    hl_appl->usr_code.code_len = user_code_len;
    // puts("User id status (hex):");
    // puts("(0) Availabe (not set), (1) Occupied");
    // puts("(2) Reserved by administrator, (fe) Status not available");
    hl_appl->usr_code.id_sts = status;
    int result = hl_usrcod_set(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_user_code_set with error :%d",result);
    }

    return result;
}

/**
hl_usrcod_sup_cb - Report callback for number of supported user codes
@param[in]  ifd         interface
@param[in]  usr_num     number of supported user codes
*/
static void hl_usrcod_sup_cb(zwifd_p ifd, uint8_t  usr_num)
{
    ALOGI("Max. number of supported user codes:%u", usr_num);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "User Code Number Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddNumberToObject(jsonRoot, "User supported number", usr_num);

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
hl_usrcod_sup_get - Get number of supported user codes
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_usrcod_sup_get(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_usrcod_sup_get(ifd, hl_usrcod_sup_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_usrcod_sup_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_user_code_number_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_USER_CODE))
    {
        return -1;
    }

    int result = hl_usrcod_sup_get(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_user_code_number_get with error:%d",result);
    }

    return result;
}

//  Command Class Protection

const char *lprot_str[] =
{
    "Unprotected",
    "Protection by sequence",
    "No operation possible",
    "unknown"
};

const char *rfprot_str[] =
{
    "Unprotected",
    "No RF control",
    "No RF control and response",
    "unknown"
};

/**
hl_prot_rep_cb - Protection states report callback
@param[in]  ifd         interface that received the report
@param[in]  local_prot  local protection state, ZW_LPROT_XXX
@param[in]  rf_prot     RF protection state, ZW_RFPROT_XXX.
@return
*/
void hl_prot_rep_cb(zwifd_p ifd, uint8_t local_prot, uint8_t rf_prot)
{
    uint8_t state;

    state = (local_prot <= 2)? local_prot : 3;
    ALOGI("Local protection state:%u(%s)", local_prot, lprot_str[state]);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Protection State Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddNumberToObject(jsonRoot, "Local port", local_prot);
    cJSON_AddStringToObject(jsonRoot, "state", lprot_str[state]);

    state = (rf_prot <= 2)? rf_prot : 3;
    ALOGI("RF protection state:%u(%s)", rf_prot, rfprot_str[state]);
 
    cJSON_AddNumberToObject(jsonRoot, "RF Port", rf_prot);
    cJSON_AddStringToObject(jsonRoot, "state", rfprot_str[state]);   

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
hl_prot_rep_setup - Setup protection report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_prot_rep_setup(hl_appl_ctx_t  *hl_appl)
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

    result = zwif_prot_rpt_set(ifd, hl_prot_rep_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_prot_rep_setup with error:%d", result);
    }

    return result;
}

/**
hl_prot_rep_get - Get the protection states
@param[in]  hl_appl     high-level api context
@return  0 on success, negative error number on failure
*/
int hl_prot_rep_get(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_prot_get(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_prot_rep_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_protection_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_PROTECTION))
    {
        return -1;
    }

    int result = hl_prot_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_prot_rep_setup done.");
        result = hl_prot_rep_get(hl_appl);
    }
    else
    {
        ALOGE("zwcontrol_protection_get with error:%d",result);
    }

    return result;
}

/**
hl_prot_sup_rep_cb - Report callback for supported protection states
@param[in]  ifd         interface
@param[in]  sup_sta     supported Protection States
*/
void hl_prot_sup_rep_cb(zwifd_p ifd, zwprot_sup_p sup_sta)
{
    uint8_t       i;

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    ALOGI("Supported Local Protection states:");
    cJSON_AddStringToObject(jsonRoot, "MessageType", "Supported Protection State Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON * Sup_Pro_State_Array =  cJSON_CreateObject();

    if(Sup_Pro_State_Array == NULL)
    {
        return;
    }

    cJSON_AddItemToObject(jsonRoot, "Supported Local Protection States", Sup_Pro_State_Array);
    for (i=0; i<sup_sta->lprot_len; i++)
    {
        ALOGI("%s", lprot_str[sup_sta->lprot[i]]);
        cJSON_AddStringToObject(Sup_Pro_State_Array, "state", lprot_str[sup_sta->lprot[i]]);
    }

    cJSON * Sup_Pro_State_Array1 =  cJSON_CreateObject();

    if(Sup_Pro_State_Array1 == NULL)
    {
        return;
    }

    ALOGI("Supported RF Protection states:");
    cJSON_AddItemToObject(jsonRoot, "Supported RF Protection States", Sup_Pro_State_Array1);
    for (i=0; i<sup_sta->rfprot_len; i++)
    {
        ALOGI("%s", rfprot_str[sup_sta->rfprot[i]]);
        cJSON_AddStringToObject(Sup_Pro_State_Array1, "state", rfprot_str[sup_sta->rfprot[i]]);
    }

    ALOGI("Additional RF supported protection types:");

    if (sup_sta->excl_ctl)
    {
        cJSON_AddStringToObject(jsonRoot, "Additional RF Sup_Pro types", "Exclusive Control");
        ALOGI("Exclusive Control");
    }

    if (sup_sta->tmout)
    {
        cJSON_AddStringToObject(jsonRoot, "Timeout", "Yes");
        ALOGI("Timeout");
    } 

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
hl_prot_sup_get - Get supported protection states
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_prot_sup_get(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_prot_sup_get(ifd, hl_prot_sup_rep_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_prot_sup_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_supported_protection_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_PROTECTION))
    {
        return -1;
    }

    int result = hl_prot_sup_get(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_protection_supported_get with error:%d",result);
    }

    return result;
}

/**
hl_prot_set - Set the protection
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_prot_set(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_prot_set(ifd, hl_appl->local_prot, hl_appl->rf_prot);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_prot_set with error:%d", result);
    }

    return result;
}

int  zwcontrol_protection_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t local_prot, uint8_t rf_prot)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_PROTECTION))
    {
        return -1;
    }

    // Local Protection State
    // (0) Unprotected (1) Protection by sequence (2) No operation possible
    hl_appl->local_prot = local_prot;

    // RF Protection State (version 2)
    // (0) Unprotected (1) No RF control (2) No RF control and response
    hl_appl->rf_prot = rf_prot;

    int result = hl_prot_set(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_protection_set with error:%d",result);
    }

    return result;
}

/**
hl_prot_ec_rep_cb - Protection exclusive control node report callback
@param[in]  ifd         interface that received the report
@param[in]  node_id     node ID that has exclusive control can override the RF protection state
                        of the device and can control it regardless of the protection state.
                        Node id of zero is used to reset the protection exclusive control state.
@return
*/
void hl_prot_ec_rep_cb(zwifd_p ifd, uint8_t node_id)
{
    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Protection Ec Control Node Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    if (node_id == 0)
    {
        ALOGI("Protection exclusive control is inactive");
        cJSON_AddStringToObject(jsonRoot, "Control Node", "inactive");
    }
    else
    {
        ALOGI("Protection exclusive control node:%u", node_id);
        cJSON_AddNumberToObject(jsonRoot, "Control Node", node_id);
    }
    
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
hl_prot_ec_rep_setup - Setup protection ec control node report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_prot_ec_rep_setup(hl_appl_ctx_t  *hl_appl)
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

    result = zwif_prot_ec_rpt_set(ifd, hl_prot_ec_rep_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_prot_ec_rep_setup with error:%d", result);
    }

    return result;
}

/**
hl_prot_ec_rep_get - Get the protection exclusive control node
@param[in]  hl_appl     high-level api context
@return  0 on success, negative error number on failure
*/
int hl_prot_ec_rep_get(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_prot_ec_get(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_prot_ec_rep_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_protection_exclusive_control_node_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_PROTECTION))
    {
        return -1;
    }

    int result = hl_prot_ec_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_prot_ec_rep_setup done.");
        result = hl_prot_ec_rep_get(hl_appl);
    }
    else
    {
        ALOGE("zwcontrol_protection_exclusive_control_node_get with error:%d",result);
    }

    return result;
}

/**
hl_prot_ec_set - Set the protection exclusive control node
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_prot_ec_set(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_prot_ec_set(ifd, hl_appl->node_id);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_prot_ec_set with error:%d", result);
    }

    return result;
}

int  zwcontrol_protection_exclusive_control_node_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t node_id)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_PROTECTION))
    {
        return -1;
    }

    hl_appl->node_id = node_id;
                
    int result = hl_prot_ec_set(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_protection_exclusive_control_node_set with error:%d",result);
    }

    return result;
}

/**
hl_prot_tmout_rep_cb - RF protection timeout report callback
@param[in]  ifd         interface that received the report
@param[in]  remain_tm   remaining time. 0x00 = No timer is set. All “normal operation” Commands must be accepted.
                        0x01 to 0x3C = 1 second (0x01) to 60 seconds (0x3C);
                        0x41 to 0xFE = 2 minutes (0x41) to 191 minutes (0xFE);
                        0xFF = No Timeout - The Device will remain in RF Protection mode infinitely.
@return
*/
void hl_prot_tmout_rep_cb(zwifd_p ifd, uint8_t remain_tm)
{

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();
    cJSON_AddStringToObject(jsonRoot, "MessageType", "Protection Timeout Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    if(jsonRoot == NULL)
    {
        return;
    }

    if (remain_tm == 0)
    {
        ALOGI("No timer is set for RF protection timeout");
        cJSON_AddStringToObject(jsonRoot, "timeout", "No timer set for RF"); 
    }
    else if ((remain_tm > 0) && (remain_tm <= 0x3C))
    {
        ALOGI("RF protection will timeout in %u seconds", remain_tm);
        cJSON_AddNumberToObject(jsonRoot, "will timeout(s)", remain_tm);
    }
    else if ((remain_tm >= 0x41) && (remain_tm <= 0xFE))
    {
        ALOGI("RF protection will timeout in %u minutes", remain_tm - 63);
        cJSON_AddNumberToObject(jsonRoot, "will timeout(m)", remain_tm - 63);
    }
    else if (remain_tm == 0xFF)
    {
        ALOGI("RF protection is always on");
        cJSON_AddStringToObject(jsonRoot, "timeout", "always on");
    }  

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
hl_prot_timeout_rep_setup - Setup protection timeout report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_prot_timeout_rep_setup(hl_appl_ctx_t  *hl_appl)
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

    result = zwif_prot_tmout_rpt_set(ifd, hl_prot_tmout_rep_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_prot_rep_setup with error:%d", result);
    }

    return result;
}

/**
hl_prot_tmout_rep_get - Get the RF protection timeout
@param[in]  hl_appl     high-level api context
@return  0 on success, negative error number on failure
*/
int hl_prot_tmout_rep_get(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_prot_tmout_get(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_prot_tmout_rep_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_protection_timeout_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_PROTECTION))
    {
        return -1;
    }

    int result = hl_prot_timeout_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_prot_timeout_rep_setup done.");
        result = hl_prot_tmout_rep_get(hl_appl);
    }
    else
    {
        ALOGE("zwcontrol_protection_timeout_get with error:%d",result);
    }

    return result;
}

/**
hl_prot_tmout_set - Set the RF protection timeout
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_prot_tmout_set(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_prot_tmout_set(ifd, hl_appl->time);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_prot_tmout_set with error:%d", result);
    }

    return result;
}

int  zwcontrol_protection_timeout_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t unit, uint8_t time)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_PROTECTION))
    {
        return -1;
    }

    //unsigned    time = 0x40;

    // Timeout unit
    // (0) seconds (1 to 60) 
    // (1) minutes (2 to 191) 
    // (2) No timeout (always protected)
    if (unit == 2)
    {   //No timeout
        hl_appl->time = 0xFF;
    }
    else
    {
        //time = prompt_uint("Timeout:");
        if (unit == 0)
        {   //Seconds
            if ((time > 0) && (time <= 60))
            {
                hl_appl->time = time;
            }
        }
        else
        {   //Minutes
            if ((time >= 2) && (time <= 191))
            {
                hl_appl->time = time + 63;
            }
        }
    }

    int result = hl_prot_tmout_set(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_protection_timeout_set with error: %d",result);
    }

    return result;
}

//  Command Class Indicator v1

/**
hl_ind_report_cb - Indicator report callback
@param[in]  ifd     The interface that received the report
@param[in]  value   The value
@return
*/
void hl_ind_report_cb(zwifd_p ifd, uint8_t value)
{
    ALOGI("Indicator value is %02Xh", value);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Indicator Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddNumberToObject(jsonRoot, "value", value);

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
hl_ind_rep_setup - Setup indicator report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_ind_rep_setup(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_ind_rpt_set(ifd, hl_ind_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_ind_rep_setup with error:%d", result);
    }

    return result;
}

/**
hl_ind_rep_get - Get indicator report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_ind_rep_get(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_ind_get(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_ind_rep_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_indicator_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_INDICATOR))
    {
        return -1;
    }

    int result = hl_ind_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_ind_rep_setup done.");
        result = hl_ind_rep_get(hl_appl);
    }
    else
    {
        ALOGE("zwcontrol_indicator_get with error: %d",result);
    }

    return result;
}

/**
hl_ind_set - indicator set value
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_ind_set(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_ind_set(ifd, (uint8_t)hl_appl->ind_val);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_ind_set with error:%d", result);
    }

    return result;
}

int  zwcontrol_indicator_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint16_t value)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_INDICATOR))
    {
        return -1;
    }

    // Value (hex)
    // Note: 00 = off, disable; FF = on, enable; Other value: 1 to 63h
    hl_appl->ind_val = value;

    int result = hl_ind_set(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_indicator_set with error: %d",result);
    }

    return result;
}

//  Command Class Door Lock Looging

/**
hl_door_lock_sup_report_cb - Door lock supported records report callback
@param[in]  ifd     The interface that received the report
@param[in]  value   The value
@return
*/
void hl_door_lock_sup_report_cb(zwifd_p ifd, uint8_t number)
{
    ALOGI("Supported records number is %02x", number);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "DoorLock Supported Records Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddNumberToObject(jsonRoot, "Supported numbers", number);

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
hl_door_lock_sup_rep_setup - Setup door lock supported records report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_door_lock_sup_rep_setup_and_get(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_drlog_rec_sup_get(ifd, hl_door_lock_sup_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_drlog_rec_sup_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_door_lock_logging_supported_records_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_DOOR_LOCK_LOGGING))
    {
        return -1;
    }

    int result = hl_door_lock_sup_rep_setup_and_get(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_door_lock_logging_supported_records_get with error: %d",result);
    }

    return result;
}

/**
hl_door_lock_rec_report_cb - Door lock records report callback
@param[in]  ifd     The interface that received the report
@param[in]  zwdrlog_rec_t   The value
@return
*/
void hl_door_lock_rec_report_cb(zwifd_p ifd, zwdrlog_rec_t *rec)
{
    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "DoorLock Logging Record Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    ALOGI("Door lock logging record number: %02x",rec->rec_num);
    cJSON_AddNumberToObject(jsonRoot, "Record Number", rec->rec_num);

    if(rec->rec_valid)
    {
        cJSON *invalid_data = cJSON_CreateObject();
        if(invalid_data == NULL)
        {
            return;
        }
        cJSON_AddItemToObject(jsonRoot, "Timestamp", invalid_data);
        cJSON_AddNumberToObject(invalid_data, "Year", rec->year);
        cJSON_AddNumberToObject(invalid_data, "Month", rec->month);
        cJSON_AddNumberToObject(invalid_data, "Day", rec->day);
        cJSON_AddNumberToObject(invalid_data, "Hour", rec->hour);
        cJSON_AddNumberToObject(invalid_data, "min", rec->min);
        cJSON_AddNumberToObject(invalid_data, "second", rec->second);
        cJSON_AddNumberToObject(invalid_data, "event", rec->evt);
        ALOGI("Door lock logging timestamp: %d:%d:%d-%d:%d:%d,",rec->year,rec->month,rec->day,rec->hour,
              rec->min, rec->second);
        ALOGI("event: %02x",rec->evt);

        char usr_code[(rec->usr_code_len) + 1];

        memcpy(usr_code, rec->usr_code, rec->usr_code_len);
        usr_code[(rec->usr_code_len)] = '\0';
        ALOGI("user id: %d, code is: %s",rec->usr_id, usr_code);
        cJSON_AddNumberToObject(jsonRoot, "User id", rec->usr_id);
        cJSON_AddStringToObject(jsonRoot, "Code", usr_code);
    }
    else
    {
        ALOGW("The requested record holds invalid data!!");
        cJSON_AddStringToObject(jsonRoot, "Message", "invalid data");
    }

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
hl_door_lock_record_rep_setup - Setup door lock records report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_door_lock_record_rep_setup(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_door_lock_record_rep_set(ifd, hl_door_lock_rec_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_door_lock_record_rep_set with error:%d", result);
    }

    return result;
}

/**
hl_door_lock_record_rep_get - Get door lock logging records report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_door_lock_record_rep_get(hl_appl_ctx_t *hl_appl, uint8_t record_number)
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

    result = zwif_drlog_rec_get(ifd, (uint8_t)record_number);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_drlog_rec_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_door_lock_logging_records_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t record_number)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_DOOR_LOCK_LOGGING))
    {
        return -1;
    }

    int result = hl_door_lock_record_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_door_lock_record_rep_setup done");
        result = hl_door_lock_record_rep_get(hl_appl, record_number);
    }
    else
    {
        ALOGE("zwcontrol_door_lock_logging_records_get with error: %d",result);
    }

    return result;
}

// Command Class Language

void hl_lang_report_cb(zwifd_p ifd, zwlang_rep_t *lang)
{
    ALOGI("supported language :%s, country:%s", lang->language, lang->country);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Language Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddStringToObject(jsonRoot, "Language", lang->language);
    cJSON_AddStringToObject(jsonRoot, "Country", lang->country); 

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
hl_lang_rep_setup - Setup language get report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_lang_rep_setup(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_lang_rep_set(ifd, hl_lang_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_lang_rep_set with error:%d", result);
    }

    return result;
}

/**
hl_lang_rep_get - Get language report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_lang_rep_get(hl_appl_ctx_t *hl_appl)
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

    result = zwif_lang_get(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_lang_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_language_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_LANGUAGE))
    {
        return -1;
    }

    int result = hl_lang_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_lang_rep_setup done.");
        result = hl_lang_rep_get(hl_appl);
    }
    else 
    {
        ALOGE("zwcontrol_language_get wit error: %d",result);
    }

    return result;
}

// Command Class Switch Color

const char *color_comp[] =
{
    "Warm Write",  // 0x00 - 0xFF: 0 - 100%
    "Cold Write",
    "Red",
    "Green",
    "Blue",
    "Amber",
    "Cyan",
    "Purple",
    "Indexed Color"  // Color Index 0-255
};

void hl_sw_color_report_cb(zwifd_p ifd, uint8_t compid, uint8_t value)
{
    ALOGI("Switch color component id:%u, value:%u", compid, value);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Switch Color Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddStringToObject(jsonRoot, "component id", color_comp[compid]);
    cJSON_AddNumberToObject(jsonRoot, "value", value);

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
hl_sw_color_rep_setup - Setup switch color get report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_sw_color_rep_setup(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_sw_color_rep_set(ifd, hl_sw_color_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_sw_color_rep_set with error:%d", result);
    }

    return result;
}

/**
hl_sw_color_rep_get - Get switch color report
@param[in]  hl_appl     The high-level api context
@param[in]  compid      Color component id
@return  0 on success, negative error number on failure
*/
int hl_sw_color_rep_get(hl_appl_ctx_t *hl_appl, uint8_t compid)
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

    result = zwif_sw_color_get(ifd, (uint8_t)compid);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_sw_color_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_switch_color_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t compId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_COLOR))
    {
        return -1;
    }

    int result = hl_sw_color_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_lang_rep_setup done.");
        result = hl_sw_color_rep_get(hl_appl, (uint8_t)compId);
    }
    else 
    {
        ALOGE("zwcontrol_switch_color_get with error: %d",result);
    }

    return result;
}

void hl_sw_color_sup_report_cb(zwifd_p ifd, uint8_t color_number, uint8_t* color_type)
{
    ALOGI("switch color supported report");

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }


    cJSON_AddStringToObject(jsonRoot, "MessageType", "Supported Color Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    if (color_number > 0)
    {
        int i;
        ALOGI("Supported Color types:");
        cJSON *supported_color;
        supported_color = cJSON_CreateObject();

        if(supported_color == NULL)
        {
            return;
        }

        cJSON_AddItemToObject(jsonRoot, "Supported Color", supported_color);

        for (i=0; i<color_number; i++)
        {
            if (color_type[i] > 20)
            {
                color_type[i] = 0;
            }

            ALOGI("%s", color_comp[color_type[i]]);
            cJSON_AddStringToObject(supported_color, "color", color_comp[color_type[i]]);
        }
    }

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
hl_sw_color_sup_rep_setup - Setup switch color supported get report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_sw_color_sup_rep_setup(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_sw_color_sup_rep_set(ifd, hl_sw_color_sup_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_sw_color_sup_rep_set with error:%d", result);
    }

    return result;
}

/**
hl_sw_color_sup_rep_get - Get supported switch color report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_sw_color_sup_rep_get(hl_appl_ctx_t *hl_appl)
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

    result = zwif_sw_color_sup_get(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_sw_color_sup_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_switch_color_supported_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_COLOR))
    {
        return -1;
    }

    int result = hl_sw_color_sup_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_sw_color_sup_rep_setup done");
        result = hl_sw_color_sup_rep_get(hl_appl);
    }
    else 
    {
        ALOGE("zwcontrol_switch_color_supported_get with error: %d",result);
    }

    return result;
}

/**
hl_sw_color_set - switch color set value
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_sw_color_set(hl_appl_ctx_t   *hl_appl, uint8_t compid, uint8_t value)
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

    result = zwif_sw_color_set(ifd, (uint8_t)compid, (uint8_t)value);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_sw_color_set with error:%d", result);
    }

    return result;
}

int  zwcontrol_switch_color_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t compId, uint8_t value)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_COLOR))
    {
        return -1;
    }

    int result = hl_sw_color_set(hl_appl, compId, value);
    if(result != 0)
    {
        ALOGE("zwcontrol_switch_color_set with error: %d",result);
    }

    return result;
}

/**
hl_sw_color_lvl_chg - toggle between start and stop switch color level change
@param[in]  hl_appl        The high-level api context
@param[in]  dir            The level change direction, 0 for increasing, 1 for decreasing
@param[in]  ignore_start   Device should respect the Start Level if the Ignore Start Level bit is 0.
@param[in]  color_id       Color component id
@param[in]  start_level    level change start level value
@return
*/
void    hl_sw_color_lvl_chg(hl_appl_ctx_t   *hl_appl, uint8_t dir, uint8_t ignore_start,
                            uint8_t color_id, uint8_t start_level)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->temp_desc);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        ALOGE("hl_sw_color_lvl_chg: interface descriptor:%u not found", hl_appl->temp_desc);
        return ;
    }

    if (hl_appl->sw_color_lvl_change_started == 0)
    {
        zwcolor_level_t lvl_ctl;

        lvl_ctl.pri_dir = dir;
        lvl_ctl.pri_ignore_lvl = (uint8_t)ignore_start;
        lvl_ctl.colorId = color_id;
        lvl_ctl.pri_level = (uint8_t)start_level;

        //Change state to start level change
        result = zwif_sw_color_level_start(ifd, &lvl_ctl);

        plt_mtx_ulck(hl_appl->desc_cont_mtx);

        if (result != 0)
        {
            ALOGE("zwif_sw_color_level_start with error:%d", result);
            return;
        }

        ALOGI("Start switch color level change ...");
        hl_appl->sw_color_lvl_change_started = 1;
    }
    else
    {
        //Change state to stop level change
        result = zwif_sw_color_level_stop(ifd);

        plt_mtx_ulck(hl_appl->desc_cont_mtx);

        if (result != 0)
        {
            ALOGE("zwif_sw_color_level_stop with error:%d", result);
            return;
        }

        ALOGI("Stop switch color level change ...");
        hl_appl->sw_color_lvl_change_started = 0;
    }
}

int  zwcontrol_start_stop_color_levelchange(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t dir, uint8_t ignore_start,
                                            uint8_t color_id, uint8_t start_level)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_COLOR))
    {
        return -1;
    }

    ALOGD("Switch color, color id:%d, dir:%d, ignore_start:%d, star_lvl:%d",
           color_id, dir, ignore_start, start_level);

    if(hl_appl->sw_color_lvl_change_started == 1){
        ALOGI("switch color level change already started, now stop level change");
        // hl_multi_lvl_chg(hl_appl);
        hl_sw_color_lvl_chg(hl_appl, dir, ignore_start, color_id, start_level);
        return 0;
    }

    hl_sw_color_lvl_chg(hl_appl, dir, ignore_start, color_id, start_level);

    return 0;

}

/*
 **  Command Class Barrier Operator
 **  Be used to control and query the status of motorized barriers.
 */

/**
hl_barrier_op_set - barrier operator set target value
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_barrier_op_set(hl_appl_ctx_t   *hl_appl, uint8_t value)
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

    result = zwif_barrier_op_set(ifd, (uint8_t)value);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_barrier_op_set with error: %d", result);
    }

    return result;
}

int  zwcontrol_barrier_operator_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t value)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_BARRIER_OPERATOR))
    {
        return -1;
    }

    int result = hl_barrier_op_set(hl_appl, value);
    if(result != 0)
    {
        ALOGE("zwcontrol_barrier_operator_set with error: %d",result);
    }

    return result;
}


void hl_barrier_op_report_cb(zwifd_p ifd, uint8_t state)
{
    ALOGI("barrier operator state is: %d",state);
}

/**
hl_barrier_op_rep_setup - Setup barrier operator get report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_barrier_op_rep_setup(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_barrier_op_rep_set(ifd, hl_barrier_op_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_barrier_op_rep_set with error:%d", result);
    }

    return result;
}

/**
hl_barrier_op_rep_get - Get barrier operator report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_barrier_op_rep_get(hl_appl_ctx_t *hl_appl)
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

    result = zwif_barrier_op_get(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_barrier_op_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_barrier_operator_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_BARRIER_OPERATOR))
    {
        return -1;
    }

    int result = hl_barrier_op_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_barrier_op_rep_setup done");
        result = hl_barrier_op_rep_get(hl_appl);
    }
    else
    {
        ALOGE("zwcontrol_barrier_operator_get with error: %d",result);
    }

    return result;
}

/**
hl_barrier_op_sig_set - barrier operator set subsystem type 
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_barrier_op_sig_set(hl_appl_ctx_t   *hl_appl, uint8_t subSysType, uint8_t state)
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

    result = zwif_barrier_op_sig_set(ifd, (uint8_t)subSysType, (uint8_t)state);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_barrier_op_sig_set with error: %d", result);
    }

    return result;
}

int  zwcontrol_barrier_operator_signal_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t subSysType, uint8_t state)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_BARRIER_OPERATOR))
    {
        return -1;
    }

    int result = hl_barrier_op_sig_set(hl_appl, subSysType, state);
    if(result != 0)
    {
        ALOGE("zwcontrol_barrier_operator_signal_set with error: %d",result);
    }

    return result;
}

void hl_barrier_op_sig_report_cb(zwifd_p ifd, uint8_t subSysType, uint8_t state)
{
    ALOGI("Barrier operator signal report: subsystem type:%u, state:%u", subSysType, state);
}

/**
hl_barrier_op_sig_rep_setup - Setup barrier operator signal get report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_barrier_op_sig_rep_setup(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_barrier_op_sig_rep_set(ifd, hl_barrier_op_sig_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_barrier_op_sig_rep_set with error:%d", result);
    }

    return result;
}

/**
hl_barrier_op_sig_rep_get - Get barrier operator signal report
@param[in]  hl_appl     The high-level api context
@param[in]  subSysType  subsystem type
@return  0 on success, negative error number on failure
*/
int hl_barrier_op_sig_rep_get(hl_appl_ctx_t *hl_appl, uint8_t subSysType)
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

    result = zwif_barrier_op_sig_get(ifd, subSysType);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_barrier_op_sig_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_barrier_operator_signal_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t subSysType)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_BARRIER_OPERATOR))
    {
        return -1;
    }

    int result = hl_barrier_op_sig_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_barrier_op_sig_rep_setup done");
        result = hl_barrier_op_sig_rep_get(hl_appl, subSysType);
    }
    else
    {
        ALOGE("zwcontrol_barrier_operator_get with error: %d",result);
    }

    return result;
}

const char *barrier_type_str[] =
{
    "Not supported",
    "Audible Notification subsystem",
    "Visual Notification subsystem"
};

/**
hl_barrier_op_sig_sup_report_cb - Report callback for supported barrier op types
@param[in]  ifd         interface
@param[in]  type_len    size of barrier op supported type buffer
@param[in]  type        buffer to store supported barrier op types
*/
void hl_barrier_op_sig_sup_report_cb(zwifd_p ifd, uint8_t type_len, uint8_t *type)
{
    if (type_len > 0)
    {
        int i;
        ALOGI("Z-wave barrier operator types:");
        for (i=0; i<type_len; i++)
        {
            if (type[i] > 3)
            {
                type[i] = 0;
            }

            ALOGI("%s", barrier_type_str[type[i]]);
        }
    }
}

/**
hl_barrier_op_sig_sup_rep_setup - Setup barrier operator signal supported get report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_barrier_op_sig_sup_rep_setup(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_barrier_op_sig_sup_rep_set(ifd, hl_barrier_op_sig_sup_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_barrier_op_sig_sup_rep_set with error:%d", result);
    }

    return result;
}

/**
hl_barrier_op_sig_sup_rep_get - Get barrier operator signal supported report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_barrier_op_sig_sup_rep_get(hl_appl_ctx_t *hl_appl)
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

    result = zwif_barrier_op_sig_sup_get(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_barrier_op_sig_sup_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_barrier_operator_signal_supported_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_BARRIER_OPERATOR))
    {
        return -1;
    }

    int result = hl_barrier_op_sig_sup_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_barrier_op_sig_sup_rep_setup done");
        result = hl_barrier_op_sig_sup_rep_get(hl_appl);
    }
    else
    {
        ALOGE("zwcontrol_barrier_operator_signal_supported_get with error: %d",result);
    }

    return result;
}


/*
 **  Command Class Basic Tariff Info
 **  Be used to request current tariff information from the meter.
 */

void hl_basic_tariff_info_report_cb(zwifd_p ifd, zwbasic_tariff_info_t *value)
{
    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Basic tariff info Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    ALOGI("Basic tariff info report, supported element number: %d",(value->ele_num)+1);
    ALOGI("Total rate number: %d",value->total_rate_num);
    ALOGI("element 1 current rate in use: %02x",value->e1_cur_rate);

    cJSON_AddNumberToObject(jsonRoot, "supported element num", (value->ele_num)+1);
    cJSON_AddNumberToObject(jsonRoot, "total rate num", value->total_rate_num);
    cJSON_AddNumberToObject(jsonRoot, "E1 current rate", value->e1_cur_rate);

    int32_t ele1_value, ele2_value;
    if (!hl_int_get(value->e1_rate_consump, 4, &ele1_value))
    {   //Error, default to zero
        ele1_value = 0;
    }
    ALOGI("element 1 rate consumption: %d",ele1_value);
    cJSON_AddNumberToObject(jsonRoot, "E1 rate consumption", ele1_value);
    cJSON_AddStringToObject(jsonRoot, "consumption units", "Wh");

    // ele_num = 0 means single element
    if (value->ele_num == 1)  // Has 2 elements, report it.
    {
        ALOGI("element 2 current rate in use: %02x",value->e2_cur_rate);
        cJSON_AddNumberToObject(jsonRoot, "E2 current rate", value->e2_cur_rate);
        if (!hl_int_get(value->e2_rate_consump, 4, &ele2_value))
        {   //Error, default to zero
            ele2_value = 0;
        }
        ALOGI("element 2 rate consumption: %d",ele2_value);
        cJSON_AddNumberToObject(jsonRoot, "E2 rate consumption", ele2_value);
        cJSON_AddStringToObject(jsonRoot, "consumption units", "Wh");
    }

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

int hl_basic_tariff_info_rep_setup(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_basic_tariff_info_rep_set(ifd, hl_basic_tariff_info_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_basic_tariff_info_rep_set with error:%d", result);
    }

    return result;
}

int hl_basic_tariff_info_rep_get(hl_appl_ctx_t *hl_appl)
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

    result = zwif_basic_tariff_info_get(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_basic_tariff_info_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_basic_tariff_info_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_BASIC_TARIFF_INFO))
    {
        return -1;
    }

    int result = hl_basic_tariff_info_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGI("hl_basic_tariff_info_rep_setup done");
        result = hl_basic_tariff_info_rep_get(hl_appl);
    }
    else
    {
        ALOGE("zwcontrol_basic_tariff_info_get with error: %d",result);
    }

    return result;
}


/*
 **  Command Class Association & Multi-Channel Association
 **  
 */

/**
hl_grp_rep_cb - Group info report callback
@param[in]  ifd Interface
@param[in]  group       Grouping identifier
@param[in]  max_cnt     Maximum number of end points the grouping identifier above supports
@param[in]  cnt         The number of end points in the grouping in this report
@param[in]  ep          An array of cnt end points in the grouping
@return
*/
void hl_grp_rep_cb(zwifd_p ifd, uint8_t group, uint8_t max_cnt, uint8_t cnt, zwepd_p ep)
{
    int i;

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Group Info Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddNumberToObject(jsonRoot, "Group id", group);
    cJSON_AddNumberToObject(jsonRoot, "Max Supported endpoints", max_cnt);
    cJSON *group_members;
    group_members = cJSON_CreateObject();
    if(group_members == NULL)
    {
        return;
    }

    cJSON_AddItemToObject(jsonRoot, "Group members", group_members);

    ALOGI("Group id:%u, max supported endpoints:%u, Group members:", group, max_cnt);

    for (i=0; i<cnt; i++)
    {
        ALOGI("Node id:%u, endpoint id:%u", ep[i].nodeid, ep[i].epid);
        cJSON_AddNumberToObject(group_members, "Node id", ep[i].nodeid);
        cJSON_AddNumberToObject(group_members, "endpoint id", ep[i].epid);
    }

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
hl_grp_rep_get - Get group info report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_grp_rep_get(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->temp_desc);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_group_get(ifd, hl_appl->group_id, hl_grp_rep_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_grp_rep_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_get_group_info(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t group_id)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_ASSOCIATION))
    {
        return -1;
    }

    hl_appl->group_id = group_id;
    int result = hl_grp_rep_get(hl_appl);
    if (result != 0)
    {
        ALOGE("zwcontrol_get_group_info with error:%d",result);
    }

    return result;
}

/**
hl_grp_add - Add endpoints into group
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_grp_add(hl_appl_ctx_t   *hl_appl)
{
    int         i;
    int         result;
    uint8_t     ep_cnt;
    zwifd_p ifd;
    zwepd_t ep_desc[5];
    zwepd_p ep;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->temp_desc);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    ep_cnt = 0;
    for (i=0; i<5; i++)
    {
        ep = hl_ep_desc_get(hl_appl->desc_cont_hd, hl_appl->ep_desc_id[i]);
        if (ep)
        {
            ep_desc[ep_cnt++] = *ep;
        }
    }

    result = zwif_group_add(ifd, hl_appl->group_id, ep_desc, ep_cnt);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_grp_add with error:%d", result);
    }

    return result;
}


int  zwcontrol_add_endpoints_to_group(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t group_id, uint32_t* nodeList)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_ASSOCIATION))
    {
        return -1;
    }

    int i;

    for (i=0; i<5; i++)
    {
        hl_appl->ep_desc_id[i] = 0;
    }

    for (i=0; i<5; i++)
    {
        hl_appl->ep_desc_id[i] = &nodeList[i];
        if (hl_appl->ep_desc_id[i] == 0)
        {
            break;
        }
    }

    hl_appl->group_id = group_id;

    int result = hl_grp_add(hl_appl);
    if (result != 0)
    {
        ALOGE("zwcontrol_add_endpoint_to_group with error:%d",result);
    }

    return result;
}

/**
hl_grp_del - Delete endpoints from group
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_grp_del(hl_appl_ctx_t   *hl_appl)
{
    int         i;
    int         result;
    uint8_t     ep_cnt;
    zwifd_p ifd;
    zwepd_t ep_desc[5];
    zwepd_p ep;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->temp_desc);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    ep_cnt = 0;
    for (i=0; i<5; i++)
    {
        ep = hl_ep_desc_get(hl_appl->desc_cont_hd, hl_appl->ep_desc_id[i]);
        if (ep)
        {
            ep_desc[ep_cnt++] = *ep;
        }
    }

    result = zwif_group_del(ifd, hl_appl->group_id, ep_desc, ep_cnt);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_grp_del with error:%d", result);
    }

    return result;
}

int  zwcontrol_remove_endpoints_from_group(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t group_id, uint32_t* nodeList)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_ASSOCIATION))
    {
        return -1;
    }

    int i;

    for (i=0; i<5; i++)
    {
        hl_appl->ep_desc_id[i] = 0;
    }

    for (i=0; i<5; i++)
    {
        hl_appl->ep_desc_id[i] = &nodeList[i];
        if (hl_appl->ep_desc_id[i] == 0)
        {
            break;
        }
    }

    hl_appl->group_id = group_id;

    int result = hl_grp_del(hl_appl);
    if (result != 0)
    {
        ALOGE("zwcontrol_remove_endpoints_from_group with error:%d",result);
    }

    return result;
}

/**
hl_grp_sup_cb - max number of groupings callback
@param[in]  ifd       interface
@param[in]  max_grp   maximum number of groupings
@return
*/
void hl_grp_sup_cb(zwifd_p ifd,  uint8_t max_grp)
{
    ALOGI("Max number of groupings:%u", max_grp);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Supported Groupings Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddNumberToObject(jsonRoot, "Max number of groupings", max_grp);

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
hl_grp_sup - Get max number of groupings
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_grp_sup(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_group_sup_get(ifd, hl_grp_sup_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_group_sup_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_get_max_supported_groups(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_ASSOCIATION))
    {
        return -1;
    }

    int result = hl_grp_sup(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_get_max_supported_groups with error:%d",result);
    }

    return result;
}

/**
hl_grp_active_cb - active group callback
@param[in]  ifd     interface
@param[in]  group   current active group
@return
*/
void hl_grp_active_cb(zwifd_p ifd,  uint8_t group)
{
    ALOGI("Current active group:%u", group);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Active Groups Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddNumberToObject(jsonRoot, "Current active group", group);

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
hl_grp_specific - Get active group
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_grp_specific(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_group_actv_get(ifd, hl_grp_active_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_group_actv_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_get_specific_group(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_ASSOCIATION))
    {
        return -1;
    }

    int result = hl_grp_specific(hl_appl);
    if (result != 0)
    {
        ALOGE("zwcontrol_get_specific_group with error:%d", result);
    }

    return result;
}

// Command class notification version 4

const static char * access_control_evt[] = 
{
    "Unknown", "Manual lock operation", "Manual unlock operation", "RF lock operation", "RF unlock operation", "Keypad lock operation", "Keypad unlock operation",
    "Manual not fully locked operation", "RF not fully locked operation", "Auto lock locked operation", "Auto lock not fully locked operation",
    "Lock jammed", "All user codes deleted", "Single user code deleted", "New user code added", "New user code not added due to duplicate code",
    "Keypad temporary disabled", "Keypad busy", "New program code entered : unique code for lock configuration",
    "Manually enter user access code exceeds code limit", "Unlock by RF with invalid user code", "Locked by RF with invalid user code", "Window/door is open", "Window/door is closed", "Window/door handle is open",
    "Window/door handle is closed", "Barrier performing initialization process"
};

const static char * home_security_evt[] =
{
    "State idle", "Intrusion (location provided)", "Intrusion", "Tampering, product cover removed", "Tampering, invalid code",
    "Glass breakage (location provided)", "Glass breakage", "Motion detection (location provided)", "Motion detection(unknown location)",
    "Tampering, product moved", "Unknown event/state"
};

const static char *notif_type[] = 
{
    "Unknown", "Smoke alarm", "CO alarm", "CO2 alarm", "Heat alarm", "Water alarm", "Access control",
    "Home security", "Power Management", "system", "emergency alarm", "Clock", "Appliance", "Home health"
};

const static char *water_alarm_evt[] =
{
    "State idle", "Water leak detected (location provided)", "Water leak detected(unknown location)", "Water level dropped (location provided)",
    "Water level dropped", "Replace water filter", "Water flow alarm", "Water pressure alarm", "Water temperature alarm", "Water level alarm",
    "Sump pump active", "Sump pump failure", "Unknown event/state"
};

/**
hl_notification_set - notification command set value
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_notification_set(hl_appl_ctx_t   *hl_appl, uint8_t notificationType, uint8_t status)
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

    result = zwif_notification_set(ifd, (uint8_t)notificationType, (uint8_t)status);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGW("hl_notification_set with error:%d", result);
    }

    return result;
}

int  zwcontrol_notification_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t notificationType, uint8_t status)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_NOTIFICATION_V4))
    {
        return -1;
    }
    int result = hl_notification_set(hl_appl, (uint8_t)notificationType, (uint8_t)status);
    if(result != 0)
    {
        ALOGW("hl_notification_set with error:%d",result);
    }
    return result;
}

/**
hl_notification_get_report_cb - notification command report callback
@param[in]  ifd     The interface that received the report
@param[in]  mode   Notification indicator value
@return
*/
void hl_notification_get_report_cb(zwifd_p ifd, zwnotification_p param)
{

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    ALOGD("V1 Alarm type:%x", param->type);
    ALOGD("V1 Alarm level:%x", param->level);
    ALOGD("Unsolicited notification status:%x", param->ex_status);
    ALOGD("Notification type:%x", param->ex_type);
    ALOGD("Notification event:%x", param->ex_event);

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Notification Get Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

/*    cJSON_AddNumberToObject(jsonRoot, "Alarm-type", param->type);
    cJSON_AddNumberToObject(jsonRoot, "Alarm-level", param->level);*/
    cJSON_AddNumberToObject(jsonRoot, "Notification-status", param->ex_status);
    cJSON_AddStringToObject(jsonRoot, "Notification-type", notif_type[param->ex_type]);
    if(param->ex_type == 0x06)
    {
        cJSON_AddStringToObject(jsonRoot, "Notification-event", access_control_evt[param->ex_event]);
    }
    else if (param->ex_type == 0x07)
    {
        cJSON_AddStringToObject(jsonRoot, "Notification-event", home_security_evt[param->ex_event]);
    }
    else if (param->ex_type == 0x05)
    {
        cJSON_AddStringToObject(jsonRoot, "Notification-event", water_alarm_evt[param->ex_event]);
    }
    /*cJSON_AddNumberToObject(jsonRoot, "Notification-event-length", param->ex_evt_len);
    cJSON_AddNumberToObject(jsonRoot, "Notification-event-param-type", param->ex_evt_type);*/
    //cJSON_AddNumberToObject(jsonRoot, "Notification-param", param->ex_evt_prm);

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
hl_notification_get_rep_setup - Setup notification get command report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_notification_get_rep_setup(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_notification_rpt_set(ifd, hl_notification_get_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_notification_get_rep_setup with error:%d for set up notification get report",
                        result);
    }

    return result;
}

/**
hl_notification_rep_get - Get the notification
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_notification_rep_get(hl_appl_ctx_t   *hl_appl, uint8_t alarmType, uint8_t notificationType, uint8_t state)
{
    int     result;
    zwifd_p ifd;

    ALOGD("notification report get,hl_appl->dst_desc_id= %d",hl_appl->dst_desc_id);
    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_notification_get(ifd, (uint8_t) alarmType, (uint8_t) notificationType, (uint8_t) state);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result == 1)
    {
        ALOGE("zwif_notification_get command queued.");
    }
    else if (result != 0)
    {
        ALOGE("zwif_notification_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_notification_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t alarmType, uint8_t notificationType, uint8_t state)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_NOTIFICATION_V4))
    {
        return -1;
    }
    int result = hl_notification_get_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGW("hl_notification_set done, alarmType:%x, notificationType:%x, event:%x", alarmType, notificationType, state);
        result = hl_notification_rep_get(hl_appl, (uint8_t) alarmType, (uint8_t) notificationType, (uint8_t) state);
    }
    return result;
}

void hl_notification_sup_get_report_cb(zwifd_p ifd, uint8_t have_vtype, uint8_t ztype_len, uint8_t *ztype)
{
    ALOGI("Notification supported report, valarm type:%u, type len:%d, ", have_vtype, ztype_len);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Notification Supported Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddNumberToObject(jsonRoot, "Have alarm type", have_vtype);

    cJSON *notification_type;
    notification_type = cJSON_CreateObject();

    if(notification_type == NULL)
    {
        return;
    }

    cJSON_AddItemToObject(jsonRoot, "supported notification",notification_type);
    for (int i = 0; i< ztype_len; i++)
    {
        ALOGI("Supported notification type is:%s", notif_type[ztype[i]]);
        cJSON_AddStringToObject(notification_type, "type", notif_type[ztype[i]]);
    }

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
hl_notification_sup_get - Setup notification supported get
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_notification_sup_get(hl_appl_ctx_t   *hl_appl)
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

    result = zwif_notification_sup_get(ifd, hl_notification_sup_get_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_notification_sup_get with error:%d",result);
    }

    return result;
}

int  zwcontrol_notification_supported_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_NOTIFICATION_V4))
    {
        return -1;
    }

    int result = hl_notification_sup_get(hl_appl);
    if (result != 0)
    {
        ALOGE("zwcontrol_notification_supported_get with error:%d",result);
    }

    return result;
}

void hl_notification_sup_evt_get_report_cb(zwifd_p ifd, uint8_t ztype, uint8_t evt_len, uint8_t *evt)
{
    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Supported Notification Event Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddStringToObject(jsonRoot, "Notification Type", notif_type[ztype]);

    ALOGI("Supported notification event report, type: %s , len:%d ",notif_type[ztype], evt_len);
    if (ztype == 0x06)
    {
        for (int i =0; i< evt_len; i++){
            ALOGI("supported Access control event: %s",access_control_evt[evt[i]]);
            cJSON_AddStringToObject(jsonRoot, "event", access_control_evt[evt[i]]);
        }
    }
    else if (ztype == 0x07)
    {
        for (int i = 0; i< evt_len;i++)
        {
            ALOGI("supported home security event: %s", home_security_evt[evt[i]]);
            cJSON_AddStringToObject(jsonRoot, "event", home_security_evt[evt[i]]);
        }
    }
    else if (ztype == 0x05)
    {
        for (int i = 0; i<evt_len; i++)
        {
            ALOGI("supported water alarm event: %s", water_alarm_evt[evt[i]]);
            cJSON_AddStringToObject(jsonRoot, "event", water_alarm_evt[evt[i]]);
        }
    }

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
hl_notification_sup_evt_get - Setup notification supported event get
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_notification_sup_evt_get(hl_appl_ctx_t   *hl_appl, uint8_t ztype)
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

    result = zwif_notification_sup_evt_get(ifd, ztype, hl_notification_sup_evt_get_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_notification_sup_evt_get with error:%d",result);
    }

    return result;
}

int  zwcontrol_notification_supported_event_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t notificationType)
{
    if(!hl_appl->is_init_done)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_NOTIFICATION_V4))
    {
        return -1;
    }

    int result = hl_notification_sup_evt_get(hl_appl, notificationType);
    if (result != 0)
    {
        ALOGE("zwcontrol_notification_supported_event_get with error:%d",result);
    }

    return result;
}