package jna;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.platform.win32.COM.COMInvoker;

/**
 * Implementation of the ITaskbarList3 interface using JNA.
 * <p>
 * This class provides a Java interface to interact with the Windows Taskbar via
 * COM using the ITaskbarList3 interface.
 * </p>
 * 
 * @author SDIDSA
 */
public final class TaskbarList3 extends COMInvoker implements ITaskbarList3 {

	/**
	 * Creates a new TaskbarList3 instance with the provided pointer.
	 *
	 * @param pointer The pointer to the native COM object.
	 */
	public TaskbarList3(Pointer pointer) {
		setPointer(pointer);
	}

	/**
	 * Sets the progress state of the taskbar button.
	 *
	 * @param hwnd     The handle to the window whose associated taskbar button's
	 *                 state is to be set.
	 * @param tbpFlags The flags indicating the progress state.
	 * @return An HRESULT value indicating success or failure.
	 */
	@Override
	public HRESULT SetProgressState(HWND hwnd, int tbpFlags) {
		// Magic number 10: Gathered by trial and error for the _invokeNativeObject
		// method.
		return (HRESULT) this._invokeNativeObject(10, new Object[] { this.getPointer(), hwnd, tbpFlags },
				HRESULT.class);
	}

	/**
	 * Sets the progress value of the taskbar button.
	 *
	 * @param hwnd     The handle to the window whose associated taskbar button's
	 *                 progress value is to be set.
	 * @param value    The current progress value.
	 * @param maxValue The maximum progress value.
	 * @return An HRESULT value indicating success or failure.
	 */
	@Override
	public HRESULT SetProgressValue(HWND hwnd, long value, long maxValue) {
		// Magic number 9: Gathered by trial and error for the _invokeNativeObject
		// method.
		return (HRESULT) this._invokeNativeObject(9, new Object[] { this.getPointer(), hwnd, value, maxValue },
				HRESULT.class);
	}
}
