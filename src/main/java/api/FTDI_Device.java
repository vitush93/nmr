package api;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface FTDI_Device extends Library {

    FTDI_Device INSTANCE = (FTDI_Device) Native.loadLibrary("usb-regulator-utlumu/device.dll", FTDI_Device.class);

    String device_set_attenuation(int attenuation);
}
