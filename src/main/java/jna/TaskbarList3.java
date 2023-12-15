package jna;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.platform.win32.COM.COMInvoker;

public final class TaskbarList3 extends COMInvoker implements ITaskbarList3 {

	public TaskbarList3(Pointer pointer) {
		setPointer(pointer);
	}

	@Override
	public HRESULT SetProgressState(HWND hwnd, int tbpFlags) {
		return (HRESULT) this._invokeNativeObject(
				10, // magic number (gathered by trial and error)
				new Object[] { 
						this.getPointer(), 
						hwnd, 
						tbpFlags
				}, HRESULT.class);
	}

	@Override
	public HRESULT SetProgressValue(HWND hwnd, long value, long maxValue) {
		return (HRESULT) this._invokeNativeObject(
				9, // magic number (gathered by trial and error)
				new Object[] { 
						this.getPointer(), 
						hwnd, 
						value, 
						maxValue 
				}, HRESULT.class);
	}
}
