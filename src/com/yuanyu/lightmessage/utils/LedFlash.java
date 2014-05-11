package com.yuanyu.lightmessage.utils;

import android.hardware.Camera;

public enum LedFlash {
	
	INSTANCE;
	
	private Camera m_Camera = null;  
    
    public void openLightOn()    {  
        if ( m_Camera == null )  
        {  
            m_Camera = Camera.open();      
        }  
          
        Camera.Parameters parameters = m_Camera.getParameters();               
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);    
        m_Camera.setParameters( parameters ); 
    }
    
    public void closeLightOff()    {  
        if ( m_Camera == null )  
        {  
            m_Camera = Camera.open();      
        }  
          
        Camera.Parameters parameters = m_Camera.getParameters();               
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);    
        m_Camera.setParameters( parameters ); 
    }
      
    public void release()   {  
        if ( m_Camera != null )  
        {  
            m_Camera.stopPreview();  
            m_Camera.release();  
            m_Camera = null;  
        }         
    }  
}
