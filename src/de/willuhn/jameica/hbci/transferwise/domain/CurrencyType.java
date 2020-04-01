/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise.domain;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JSON-Mapping.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public enum CurrencyType
{
  /**
   * EUR
   */
  EUR,
  
  /**
   * Britische Pfund.
   */
  GBP,
  
  /**
   * US-Dollar.
   */
  USD,
  
  ;
  
  /**
   * Default-Waehrung. 
   */
  public final static CurrencyType DEFAULT = EUR;
  
  /**
   * Versucht die Waehrung aus dem Text zu ermitteln.
   * @param s die Waehrungsbezeichnung.
   * @return die Waehrung oder die Default-Waehrung, wenn keine definiert ist.
   */
  public static CurrencyType determine(String s)
  {
    s = StringUtils.trimToNull(s);
    if (s == null)
      return DEFAULT;
    
    try
    {
      return CurrencyType.valueOf(s.toUpperCase());
    }
    catch (Exception e)
    {
    }
    return DEFAULT;
  }
}


