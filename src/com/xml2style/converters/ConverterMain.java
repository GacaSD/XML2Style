package com.xml2style.converters;

import com.xml2style.XML2StyleDialog;

/**
 * Created by GacaPC on 9.2.2016..
 */
public abstract class ConverterMain {

    private XML2StyleDialog mDialog;

    public ConverterMain(XML2StyleDialog dialog) {
        mDialog = dialog;
    }

    protected XML2StyleDialog getDialog(){
        return mDialog;
    }

    public abstract void convert();

}
