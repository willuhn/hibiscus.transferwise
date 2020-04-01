/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise.domain.profiles;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JSON-Mapping.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ProfileDetails
{
  /**
   * Vorname.
   */
  public String firstName;
  
  /**
   * Nachname.
   */
  public String lastName;
  
  /**
   * Geburtsdatum.
   */
  @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
  public Date dateOfBirth;
  
  /**
   * Telefonnummer.
   */
  public String phoneNumber;
  
  /**
   * Link zum Avatar-Bild.
   */
  public String avatar;
  
  /**
   * 
   */
  public String occupation;

  /**
   * 
   */
  public String occupations;
  
  /**
   * Adress-ID.
   */
  public Integer primaryAddress; 


  // Properties von Business-Profilen
  
  /**
   * Firmenname.
   */
  public String name;
  
  /**
   * HR-Nummer.
   */
  public String registrationNumber;
  
  /**
   * 
   */
  public String acn;
  
  /**
   * 
   */
  public String abn;
  
  /**
   * 
   */
  public String arbn;
  
  /**
   * 
   */
  public String companyType;
  
  /**
   * 
   */
  public String companyRole;
  
  /**
   * 
   */
  public String descriptionOfBusiness;
  
  /**
   * 
   */
  public String webpage;
  
  /**
   * 
   */
  public String businessCategory;
  
  /**
   * 
   */
  public String businessSubCategory;

  /**
   * Liefert den Vornamen.
   * @return der Vorname.
   */
  public String getFirstName()
  {
    return firstName;
  }

  /**
   * Liefert den Nachnamen.
   * @return der Nachname.
   */
  public String getLastName()
  {
    return lastName;
  }

  /**
   * Liefert den Firmennamen.
   * @return der Firmenname.
   */
  public String getName()
  {
    return name;
  }

  
}
