package org.vm.vs.wrapper;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

import com.vmware.vim25.CustomFieldDef;
import com.vmware.vim25.CustomFieldStringValue;
import com.vmware.vim25.CustomFieldValue;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.CustomFieldsManager;
import com.vmware.vim25.mo.VirtualMachine;


/**
 * 
 * @author fender
 *
 */
public class VUtil {

	private static Logger log = Logger.getLogger(VUtil.class);
	
    /**
     * 
     * @param vm
     * @param key
     * @param value
     * @throws RuntimeFault
     * @throws RemoteException
     */
	public static void setCustomAttr(VirtualMachine vm, String key, String value) 
			throws RuntimeFault, RemoteException {
		
		vm.setCustomValue(key, value);
	}	
	
	/**
	 * 
	 * @param vm
	 * @param attrKey
	 * @return
	 * @throws InvalidProperty
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	public static CustomFieldValue getCustomAttribute(VirtualMachine vm, String attrKey) 
			throws InvalidProperty, RuntimeFault, RemoteException {
		
        CustomFieldValue[] values = vm.getCustomValue();
        CustomFieldDef[] defs = vm.getAvailableField();
        
        if (values == null || defs == null) {
        	log.debug("custom values or defs is empty.");
        	return null;
        }
        
        for (int i = 0; i < defs.length; i++) {
        	
            if (attrKey.equals(defs[i].getName())) {                
                int targetIndex = defs[i].getKey();
                
                for (int j = 0; j < values.length; j++) {
                    if (targetIndex == values[j].getKey()) {
                        return values[j];
                    }
                }
            }
        }
        
        return null;
    }
	
	/**
	 * 
	 * @param vm
	 * @param attrKey
	 * @return
	 * @throws InvalidProperty
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	public static String getCustomAttributeValue(VirtualMachine vm, String attrKey)
			throws InvalidProperty, RuntimeFault, RemoteException {
		
		CustomFieldStringValue cfv = (CustomFieldStringValue) getCustomAttribute(vm, attrKey);
		return cfv == null ? null : cfv.getValue();
	}

	/**
	 * 
	 * @param key
	 * @param vm
	 * @return
	 */
	public static String printCustomFieldValues(String key, VirtualMachine vm) {
		CustomFieldValue [] customFieldValues = vm.getCustomValue();
		
		if (customFieldValues != null) {
			for (int i = 0; i < customFieldValues.length; i++) {
				log.debug("DynamicType : " + customFieldValues[i].getKey());			
				log.debug("DynamicType : " + customFieldValues[i].getDynamicType());		
			}
		} else {
			log.debug("-- customFieldValues is empty");
		}
				
		return null;
	}

	/**
	 * Prints all the custom fields. This is for testing purposes.
	 * 
	 * @param customFieldsMgr
	 */
	public static void printCustomFields(CustomFieldsManager customFieldsMgr) {
        CustomFieldDef [] customFields = customFieldsMgr.getField();
                
        if (customFields != null) {
            for (int i = 0; i < customFields.length; i++) {
                log.debug("custom field : (int) key : " + customFields[i].getKey());
                log.debug("custom field : (str) name : " + customFields[i].getName());
                log.debug("custom field : (str) dynamic type : " + customFields[i].getDynamicType());
                log.debug("custom field : (str) type : " + customFields[i].getType());
            }
        }
	}
		
	/**
	 * 
	 * @param d
	 * @return
	 */
	public static String dateFormatter(Date d) {
		Calendar cal = GregorianCalendar.getInstance();
		
		cal.setTime(d);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int mins = cal.get(Calendar.MINUTE);
		
		String hoursStr = ""+hour;
		if(hoursStr.length() == 1)
			hoursStr = "0" + hoursStr;
		
		String minsStr = ""+mins;
		if(minsStr.length() == 1)
			minsStr = "0" + minsStr;

		return hoursStr + ":" + minsStr ; 
	}
	
	/**
	 * Returns the access control permission from the virtual machine.
	 * 
	 * @param vm
	 * @return
	 */
	public static AccessControlPermEnum getAccessControlPerm(VirtualMachine vm) {		
		try {
			AccessControlPermEnum perm = AccessControlPermEnum.USER;
			String mask = VUtil.getCustomAttributeValue(vm, VMachineInfo.VM_CFIELD_CONTROL_MASK_NAME);
			
			if (mask != null) {
				int id = Integer.valueOf(mask).intValue();
				return perm.getAccessControl(id);
			}
			
		} catch (Exception e) {
			log.info("The VM's control mask could not be retrieved", e);
		}
		
		return AccessControlPermEnum.USER;
	}
}
