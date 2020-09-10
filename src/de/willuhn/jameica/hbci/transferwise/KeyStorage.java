/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.security.crypto.AESEngine;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.Base64;
import de.willuhn.util.I18N;

/**
 * Speichert die Keys.
 */
public class KeyStorage
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  
  private final static String SUFFIX_PRIVATE = ".private";
  private final static String SUFFIX_PUBLIC  = ".public";
  
  private static Wallet wallet = null;
  
  /**
   * Liefert das Wallet.
   * @return das Wallet.
   * @throws Exception
   */
  private static synchronized Wallet getWallet() throws Exception
  {
    if (wallet == null)
      wallet = new Wallet(KeyStorage.class,new AESEngine());
    return wallet;
  }
  
  /**
   * Liefert das Schluesselpaar fuer das angegebene Konto.
   * @param k das Konto.
   * @return das Schluesselpaar oder NULL, wenn noch keines existiert.
   * @throws ApplicationException
   */
  public static KeyPair getKey(Konto k) throws ApplicationException
  {
    try
    {
      checkAccount(k);
      
      final String priv = (String) getWallet().get(k.getID() + SUFFIX_PRIVATE);
      if (StringUtils.trimToNull(priv) == null)
        return null;
      final PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(Base64.decode(priv));

      final String pub = (String) getWallet().get(k.getID() + SUFFIX_PUBLIC);
      if (StringUtils.trimToNull(pub) == null)
        return null;
      final X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Base64.decode(pub));

      final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return new KeyPair(keyFactory.generatePublic(pubSpec),keyFactory.generatePrivate(privSpec));
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to read key pair for account",e);
      throw new ApplicationException(i18n.tr("Laden des Schlüssels fehlgeschlagen: {0}",e.getMessage()));
    }
  }
  
  /**
   * Loescht den Schluessel eines Kontos.
   * @param k
   * @throws ApplicationException
   */
  public static void deleteKey(Konto k) throws ApplicationException
  {
    try
    {
      checkAccount(k);
      getWallet().delete(k.getID());
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to delete key pair for account",e);
      throw new ApplicationException(i18n.tr("Löschen des Schlüssels fehlgeschlagen: {0}",e.getMessage()));
    }
  }
  
  /**
   * Erstellt einen neuen Schluessel fuer das Konto.
   * Die Funktion speichert das Schluesselpaar auch gleich ab.
   * @param k das Konto.
   * @return der neue Schluessel.
   * @throws ApplicationException
   */
  public static synchronized KeyPair createKey(Konto k) throws ApplicationException
  {
    try
    {
      checkAccount(k);
      long started = System.currentTimeMillis();
      Logger.info("creating new key pair for account [id: " + k.getID() + "]");
      final KeyPairGenerator kp = KeyPairGenerator.getInstance("RSA",BouncyCastleProvider.PROVIDER_NAME);
      kp.initialize(4096); // Mindest-Anforderung seitens Transferwise sind 2048 Bit.
      final KeyPair keypair = kp.generateKeyPair();

      // Private-Key speichern
      {
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keypair.getPrivate().getEncoded());
        getWallet().set(k.getID() + SUFFIX_PRIVATE,Base64.encode(spec.getEncoded()));
      }
      
      // Public-Key speichern
      {
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(keypair.getPublic().getEncoded());
        getWallet().set(k.getID() + SUFFIX_PUBLIC,Base64.encode(spec.getEncoded()));
      }
      
      Logger.info("key pair created for account [id: " + k.getID() + "], took " + (System.currentTimeMillis() - started) + " millis");
      return keypair;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to create  key pair for account",e);
      throw new ApplicationException(i18n.tr("Erstellen des Schlüssels fehlgeschlagen: {0}",e.getMessage()));
    }
  }
  
  /**
   * Prueft, ob das Konto die Anforderungen erfuellt.
   * @param k das zu pruefende Konto.
   * @throws Exception
   */
  private static void checkAccount(Konto k) throws Exception
  {
    if (k == null || StringUtils.trimToNull(k.getID()) == null)
      throw new ApplicationException(i18n.tr("Kein Konto ausgewählt"));

    // Checken, ob das Konto ein Transferwise-Konto ist.
    if (!Plugin.getStatus(k).checkInitial())
      throw new ApplicationException(i18n.tr("Das Konto ist nicht für Transferwise konfiguriert"));
  }
  
}
