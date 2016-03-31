/*
 * @(#)GeometryDebyeScherrer.java created 06/01/1999 Riva del Garda
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

package it.unitn.ing.rista.diffr.geometry;

import it.unitn.ing.rista.diffr.*;
import it.unitn.ing.rista.util.Constants;
import it.unitn.ing.rista.util.MoreMath;

/**
 *  The GeometryDebyeScherrer is a class to apply correction for Debye-Scherrer
 *  geometry.
 *
 *
 * @version $Revision: 1.10 $, $Date: 2006/01/19 14:45:56 $
 * @author Luca Lutterotti
 * @since JDK1.1
 */

public class GeometryDebyeScherrer extends GeometryDiffractometer {

  public GeometryDebyeScherrer(XRDcat aobj, String alabel) {
    super(aobj, alabel);
    identifier = "Debye-Scherrer";
    IDlabel = "Debye-Scherrer";
    description = "Debye-Scherrer instrument geometry";
  }

  public GeometryDebyeScherrer(XRDcat aobj) {
    this(aobj, "Debye-Scherrer");
  }

  public GeometryDebyeScherrer(String[] labels) {
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

  public GeometryDebyeScherrer() {
    identifier = "Debye-Scherrer";
    IDlabel = "Debye-Scherrer";
    description = "Debye-Scherrer instrument geometry";
  }

  public double LorentzPolarization(DiffrDataFile adatafile, Sample asample, double position, boolean dspacingbase) {
    position *= degtopi2;
    return polarization(adatafile, position) * Lorentz(adatafile, position);
  }

  static double degtopi2 = Constants.DEGTOPI / 2.0;

  public double Lorentz(DiffrDataFile adatafile, double position) {
    double sintheta, costheta, lp;
    sintheta = Math.sin(position);
    costheta = Math.cos(position);
    lp = 0.5 / (costheta * sintheta * sintheta);
    return lp;
  }

  public double polarization(DiffrDataFile adatafile, double position) {
    if (((Instrument) getParent()).isNeutron())
      return 1.0;

    // X-ray
    double sin2theta = Math.sin(position * 2.0);
    sin2theta *= sin2theta;
    double Ph = getMonochromatorCorrection(adatafile);
    return (2.0 - Ph * sin2theta);
  }

  public void computeShapeAbsorptionCorrection(DiffrDataFile adatafile, Sample asample, float[] position,
                                               boolean dspacingbase, float[] intensity, float toLambda) {

    float[] sampleAngles = asample.getSampleAngles();
    float[] tilting_angles = adatafile.getTiltingAngle();

    float[][] angles = getIncidentAndDiffractionAngles(adatafile, tilting_angles, sampleAngles, position);

    Radiation rad = ((Instrument) getParent()).getRadiationType().getRadiation(0);

    asample.computeAbsorptionTroughPath(rad, angles, position, intensity, toLambda);
  }

}
