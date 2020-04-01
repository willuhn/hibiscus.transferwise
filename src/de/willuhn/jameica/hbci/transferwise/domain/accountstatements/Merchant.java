/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise.domain.accountstatements;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JSON-Mapping.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Merchant
{
  /**
   * Name.
   */
  public String name;
  
  /**
   * Anschrift.
   */
  public String firstLine;
  
  /**
   * PLZ.
   */
  public String postCode;
  
  /**
   * Ort.
   */
  public String city;
  
  /**
   * Staat.
   */
  public String state;
  
  /**
   * Land.
   */
  public String country;
  
  /**
   * Kategorie.
   */
  public String category;

}


