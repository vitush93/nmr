package cz.vithabada.nmr_gui.api;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Handles USB Attenuator configuration via the FTDI.
 *
 * @author Vit Habada
 */
public interface FTDI_Device extends Library {

    /**
     * API Instance
     */
    FTDI_Device INSTANCE = (FTDI_Device) Native.loadLibrary("usb-regulator-utlumu/device.dll", FTDI_Device.class);

    /**
     *
     * @param attenuation attenuation in dB
     * @return error description if any or null otherwise
     */
    String device_set_attenuation(int attenuation);
}
