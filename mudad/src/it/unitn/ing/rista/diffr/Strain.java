/*
 * @(#)Strain.java created 15/11/1999 Pergine Vals.
 *
 * Copyright (c) 1999 Luca Lutterotti All Rights Reserved.
 *
 * This software is the research result of Luca Lutterotti and it is
 * provided as it is as confidential and proprietary information.
 * You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you
 * entered into with the author.
 *
 * THE AUTHOR MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. THE AUTHOR SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 */

package it.unitn.ing.rista.diffr;

import it.unitn.ing.rista.awt.*;
import it.unitn.ing.rista.util.*;

import java.awt.*;
import java.lang.*;
import javax.swing.*;

/**
 * The Strain is a general class to obtain strain maps from spectra.
 * This class does nothing, and applied theories must be implemented in subclasses
 * of this class.
 *
 *
 * @version $Revision: 1.8 $, $Date: 2005/05/06 18:07:25 $
 * @author Luca Lutterotti
 * @since JDK1.1
 */


public class Strain extends XRDcat {

  public Strain(XRDcat aobj, String alabel) {
    super(aobj, alabel);
  }

  public Strain(XRDcat aobj) {
    this(aobj, "Strain model x");
  }

  public Strain() {
  }

  public Phase getPhase() {
    return (Phase) getParent();
  }

  public void computeStrain(Sample asample) {
    Phase aphase = getPhase();
    computeStrain(aphase, asample);
  }

  public void computeStrain(Phase aphase, Sample asample) { // you don't need to modify this unless


    if (refreshComputation) {  // the computation is done only when needed
      refreshComputation = false;  // first we set it false, so this method will not be called again if not needed

      prepareComputation(aphase, asample); // if something is needed before the real computation

      aphase.sghklcompute(false);  // will refresh the peak list if necessary
      int hkln = aphase.gethklNumber(); // we enquire to the phase the number of reflections
      for (int j = 0; j < hkln; j++) {  // loop for each reflection

        // first for each reflection and for each spectrum in each dataset we need to get the
        // texture (or strain) angles, for this we need to know the reflection position (2theta or d-space)
        Reflection refl = (Reflection) aphase.reflectionv.elementAt(j);
        double position = 0.0;
        double dspace = aphase.getDspacing(j);

        prepareComputation(refl); // if you need to do something on the hkl basis

        int datafile = 0;
        for (int i = 0; i < asample.activeDatasetsNumber(); i++) { // the loop goes only for active datasets
          DataFileSet adataset = asample.getActiveDataSet(i);

          int datafilenumber = adataset.activedatafilesnumber();  // the number of spectra in the dataset
          for (int i1 = 0; i1 < datafilenumber; i1++) { // now the loop over the number of spectra in the dataset
            DiffrDataFile adatafile = adataset.getActiveDataFile(i1);
            if (adatafile.dspacingbase)
              position = dspace;  // the position of the peak is in d-space (TOF neutron, Energy Dispersive)
            else
              position = adataset.computeposition(dspace, 0); // 2theta, we compute the 2theta peak position
            if (!adatafile.dspacingbase && position != 180) { // obviously no peak may have such 2theta position
              datafile = adatafile.getIndex();  // we need the real index  of the spectrum/datafile, remember the index
                                                // take care of inactive spectra, so it should be retrived,
                                                // it is not a progressive number
              position = adatafile.getCorrectedPosition(asample, position); // 2theta, we compute the real position
                                                                            // corrected for instrument aberrations
              float strain_angles[] = adatafile.getTextureAngles((float) position); // we inquire for the angles

              // now we compute the strain for the hkl reflection (phi and beta are the polar and azimuthal
              // crystallographic angles, the strain angles are the chi (or psi) and phi angles for strain
              // measurements (see the next method)
              double strain = computeStrain(refl.phi[0], refl.beta[0],
                    strain_angles[0] * Constants.DEGTOPI,
                    strain_angles[1] * Constants.DEGTOPI);
              refl.setStrain(datafile, strain);
              refl.setExpStrain(datafile, strain);
            }
          }
        }
      }
    }

  }

  void prepareComputation(Reflection refl) {
  }

  void prepareComputation(Phase aphase, Sample asample) {
  }

  public double computeStrain(double psi, double beta, double chi, double phi) {
    // Angles must be in radiants
    // psi and beta are the polar and azimuthal angles for the crystal setting
    // phi and chi for the sample

    return 0.0;
  }

  public double computeStrain(Phase aphase, double strain_angles[],
                              int h, int k, int l) {
    Reflection refl = aphase.getReflectionByhkl(h, k, l);
    return computeStrain(refl.phi[0], refl.beta[0],
            strain_angles[0] * Constants.DEGTOPI,
            strain_angles[1] * Constants.DEGTOPI);

  }

  public double[] computeStrain(Phase aphase, double alpha[], double beta[],
                                Reflection reflex) {

    int numberOfPoints = alpha.length;
    double[] strainValues = new double[numberOfPoints];

    for (int i = 0; i < numberOfPoints; i++) {
      strainValues[i] = computeStrain(reflex.phi[0], reflex.beta[0],
              alpha[i] * Constants.DEGTOPI,
              beta[i] * Constants.DEGTOPI);
    }

    return strainValues;
  }

  public double[][] getPoleFigureGrid(Reflection refl, int numberofPoints, double maxAngle) {

    return null;
  }

  public double[][] getExpPoleFigureGrid(Reflection reflex, int numberofPoints, double maxAngle) {

    return null;
  }

  public boolean needPositionExtractor() {
    return false;
  }

  public void notifyParameterChanged(Parameter source) {
    notifyParameterChanged(source, Constants.STRAIN_CHANGED);
  }

  public void notifyStringChanged(String source) {
    notifyStringChanged(source, Constants.STRAIN_CHANGED);
  }

  public void notifyObjectChanged(XRDcat source) {
    notifyUpObjectChanged(source, Constants.STRAIN_CHANGED);
  }

  public void refreshForNotificationUp(XRDcat source, int reason) {
    if (!getFilePar().isComputingDerivate() || source == this)
      refreshComputation = true;
  }

  public void refreshForNotificationDown(XRDcat source, int reason) {
    if (!getFilePar().isComputingDerivate() || (source == this || reason == Constants.SAMPLE_ORIENTATION_CHANGED))
      refreshComputation = true;
  }

  public JOptionsDialog getOptionsDialog(Frame parent) {
    return new JStrainOptionsD(parent, this);
  }

  public class JStrainOptionsD extends JOptionsDialog {

    public JStrainOptionsD(Frame parent, XRDcat obj) {

      super(parent, obj);

      principalPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 6));
      principalPanel.add(new JLabel("No options for this model"));

      setTitle("Strain options panel");
      pack();
    }

    public void initParameters() {
    }

    public void retrieveParameters() {
    }

  }
}
