package edu.uwm.cs.util;

public enum XMLTokenType {
     ERROR,       //!< Error
     OPEN,        //!< "<" Name
     ATTR,        //!< Name "=" AttrVal
     CLOSE,       //!< ">"
     ECLOSE,      //!< "/>"
     ETAG,        //!< "</" Name ">"
     TEXT ;        //!< plain text

}
