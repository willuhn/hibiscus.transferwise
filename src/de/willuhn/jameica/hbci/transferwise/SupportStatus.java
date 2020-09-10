/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.transferwise.synchronize.TransferwiseSynchronizeBackend;
import de.willuhn.logging.Logger;

/**
 * Fuehrt verschiedenste Pruefungen durch, um herauszufinden, inwiefern ein Konto alle Anforderungen fuer die
 * Nutzung via TransferWise erfuellt.
 */
public class SupportStatus
{
  private Konto konto = null;

  /**
   * ct.
   * @param k das Konto.
   */
  SupportStatus(Konto k)
  {
    this.konto = k;
  }
  
  /**
   * Liefert das zugehoerige Konto.
   * @return das zugehoerige Konto.
   */
  public Konto getKonto()
  {
    return this.konto;
  }
  
  /**
   * Prueft, ob die korrekte BIC hinterlegt ist.
   * @return true, wenn die korrekte BIC hinterlegt ist.
   */
  public boolean checkBic()
  {
    try
    {
      String bic = StringUtils.trimToNull(this.konto.getBic());
      if (bic == null)
        return false;
      
      bic = bic.replace(" ","");
      return bic.equalsIgnoreCase(Plugin.BIC_TRANSFERWISE);
    }
    catch (Exception e)
    {
      Logger.error("unable to check bic for account",e);
      return false;
    }
  }
  
  /**
   * Prueft, ob das korrekte Backend konfiguriert ist.
   * @return true, wenn das korrekte Backend konfiguriert ist.
   */
  public boolean checkBackend()
  {
    try
    {
      final String backend = StringUtils.trimToNull(this.konto.getBackendClass());
      return Objects.equals(backend,TransferwiseSynchronizeBackend.class.getName());
    }
    catch (Exception e)
    {
      Logger.error("unable to check backend for account",e);
      return false;
    }
  }
  
  /**
   * Prueft, ob der API-Key hinterlegt ist.
   * @return true, wenn der API-Key hinterlegt ist.
   */
  public boolean checkApiKey()
  {
    try
    {
      return StringUtils.trimToNull(this.konto.getMeta(Plugin.META_PARAM_APIKEY,null)) != null;
    }
    catch (Exception e)
    {
      Logger.error("unable to check api key for account",e);
      return false;
    }
  }
  
  /**
   * Prueft, ob das Schluesselpaar hinterlegt ist.
   * @return true, wenn das Schluesselpaar hinterlegt ist.
   */
  public boolean checkKeyPair()
  {
    try
    {
      return KeyStorage.getKey(this.konto) != null;
    }
    catch (Exception e)
    {
      Logger.error("unable to check key pair for account",e);
      return false;
    }
  }
  
  /**
   * Prueft alle Anforderungen des Kontos.
   * @return true, wenn alle Anforderungen erfuellt sind.
   */
  public boolean checkAll()
  {
    return this.checkBic() && 
           this.checkBackend() &&
           this.checkApiKey() &&
           this.checkKeyPair();
  }

  /**
   * Prueft die Minimal-Anforderungen des Kontos, um es im Sync-Backend zu verwenden.
   * @return true, wenn alle Anforderungen erfuellt sind.
   */
  public boolean checkSyncProvider()
  {
    return this.checkBic() && 
           this.checkBackend();
  }

  /**
   * Prueft die Minimal-Anforderungen des Kontos, damit es grundsaetzlich ueberhaupt als Transferwise-Konto erkannt wird.
   * @return true, wenn alle Anforderungen erfuellt sind.
   */
  public boolean checkInitial()
  {
    return this.checkBackend();
  }

}
