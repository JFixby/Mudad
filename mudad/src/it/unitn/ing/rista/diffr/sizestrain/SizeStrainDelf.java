/*
 * @(#)SizeStrainDelf.java created 05/10/1998 Verona
 *
 * Copyright (c) 1998 Luca Lutterotti All Rights Reserved.
 *
 * This software is the research result of Luca Lutterotti and it is
 * provided as it is as confidential and proprietary information.
 * You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you
 * entered into with Luca Lutterotti.
 *
 * THE AUTHOR MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. THE AUTHOR SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 */

package it.unitn.ing.rista.diffr.sizestrain;

import it.unitn.ing.rista.diffr.*;
import it.unitn.ing.rista.util.*;
import it.unitn.ing.rista.interfaces.Peak;

/**
 *  The SizeStrainDelf is a class to model profile function with the
 *  method described by Delf researchers in the Young Rietveld book.
 *
 *
 * @version $Revision: 1.5 $, $Date: 2006/01/19 14:45:58 $
 * @author Luca Lutterotti
 * @since JDK1.1
 */


public class SizeStrainDelf extends SizeStrainModel {

  public SizeStrainDelf(XRDcat aobj, String alabel) {
    super(aobj, alabel);
    initXRD();
    identifier = "Delf";
    IDlabel = "Delf";
    description = "select this to apply the Delf model";
  }

  public SizeStrainDelf(XRDcat aobj) {
    this(aobj, "Line Broadening Delf model");
  }

  public SizeStrainDelf(String[] labels) {
    this();
    if (labels != null) {
      if (labels.length > 1) {
        identifier = labels[0];
        IDlabel = labels[1];
      }
      if (labels.length > 2)
        description = labels[2];
    }
  }

  public SizeStrainDelf() {
    identifier = "Delf";
    IDlabel = "Delf";
    description = "select this to apply the Delf model";
  }

  public double getBetaChauchy(Peak peak, double cryst, double mstrain) {
    if (cryst == 0.0)
      return 0.0;
    cryst = Math.abs(cryst);
//		Misc.println(cryst);
    if (peak.getdspacingBase())
      return peak.getMeanPosition() * peak.getMeanPosition() / cryst;
    else {
      double positionTheta = peak.getMeanPosition() * 0.5;
      if (positionTheta < 90.0)
        return peak.getMeanWavelength() / (cryst * Math.cos(positionTheta * Constants.DEGTOPI))
                / Constants.DEGTOPI;
      else
        return 0.0;
    }
  }

  public double getBetaGauss(Peak peak, double cryst, double mstrain) {
    if (mstrain == 0.0)
      return 0.0;

    mstrain *= Constants.mstraintoetilde;
    mstrain = Math.abs(mstrain);
//		Misc.println(mstrain);
    if (peak.getdspacingBase())
      return 2.0 * mstrain * peak.getMeanPosition();
    else {
      double positionTheta = peak.getMeanPosition() * 0.5;
      if (positionTheta < 90.0)
        return 4.0 * mstrain * Math.tan(positionTheta * Constants.DEGTOPI)
                / Constants.DEGTOPI;
      else
        return 0.0;
    }
  }

}
