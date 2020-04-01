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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JSON-Mapping.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class UserProfile
{
  /**
   * Profil-ID.
   */
  public Integer id;
  
  /**
   * Typ des Profils.
   */
  public String type;
  
  /**
   * Profil-Details.
   */
  public ProfileDetails details;

  /**
   * Liefert die ID.
   * @return die ID.
   */
  public Integer getId()
  {
    return id;
  }

  /**
   * Liefert den Typ.
   * @return type der Typ.
   */
  public String getType()
  {
    return type;
  }

  /**
   * Liefert die Details.
   * @return details die Details.
   */
  public ProfileDetails getDetails()
  {
    return details;
  }

}
