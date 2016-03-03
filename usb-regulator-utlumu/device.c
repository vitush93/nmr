#include <ftdi.h>

#define DEVICE_API __declspec(dllexport)

#define DEVICE_VENDOR_ID 	0x0403
#define DEVICE_PRODUCT_ID 	0x6001
#define DEVICE_SERIAL 		"?" // FTS61DGW

struct ftdi_context device_context;

DEVICE_API char* device_set_attenuation(int attenuation)
{
	if (device_init() < 0) return ftdi_get_error_string(&device_context);

	if (device_connect() < 0) return ftdi_get_error_string(&device_context); 
	
	if (device_transmit(attenuation) < 0) return ftdi_get_error_string(&device_context);

	device_disconnect();

	ftdi_get_error_string(&device_context);
}

int device_init()
{
	return ftdi_init(&device_context);
}

int device_connect()
{
	char *device_serial = DEVICE_SERIAL;

	if (ftdi_usb_open_desc(&device_context, DEVICE_VENDOR_ID, DEVICE_PRODUCT_ID, NULL, device_serial) < 0) {
		return -1;
	} else {
		return ftdi_enable_bitbang(&device_context, 0xFF);
	}
}

int device_disconnect()
{
	return ftdi_usb_close(&device_context);
}

void device_reconnect()
{
	device_disconnect();
	device_connect();
}

int device_transmit(int attenuation)
{
	return ftdi_write_data(&device_context, (unsigned char*)&attenuation, 1);
}