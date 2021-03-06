/*
 * @(#)PopaBalzarHarmonicStrainModel.java created Jul 31, 2003 Berkeley
 *
 * Copyright (c) 1996-2003 Luca Lutterotti All Rights Reserved.
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

package dot.bar.foo;

import it.unitn.ing.rista.diffr.*;
import it.unitn.ing.rista.diffr.rta.PoleFigureOutput;
import it.unitn.ing.rista.util.*;
import it.unitn.ing.rista.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import java.util.Vector;
import java.awt.*;
import java.awt.event.*;


/**
 * The PopaBalzarHarmonicStrainModel is a class to compute the strain
 * based on the model published by Popa and Balzar, J. Appl. Cryst. 34
 * (2001) 187-195.
 * This example is uncompleted and not computing correctly and it is 
 * reported only as a code example. Do not use or modify it.
 *
 * @version $Revision: 1.6 $, $Date: 2006/01/19 14:45:56 $
 * @author Luca Lutterotti
 * @since JDK1.1
 */

public class PopaBalzarHarmonicStrainModel extends Strain {


  public static String[] diclistc = {"_rista_sample_symmetry",
                                     "_rista_harmonic_expansion_degree",

                                     "_rista_harmonic_strain_11",
                                     "_rista_harmonic_strain_22",
                                     "_rista_harmonic_strain_33",
                                     "_rista_harmonic_strain_23",
                                     "_rista_harmonic_strain_13",
                                     "_rista_harmonic_strain_12"
  };
  public static String[] diclistcrm = {"_rista_sample_symmetry",
                                     "_rista_harmonic_expansion_degree",

                                     "strain_11",
                                     "strain_22",
                                     "strain_33",
                                     "strain_23",
                                     "strain_13",
                                     "strain_12"
  };

  public static String[] classlistcs = {};
  public static String[] classlistc = {};
  public static String[] symmetrychoice = {"-1",
                                           "2/m",
                                           "2/mmm",
                                           "4/m",
                                           "4/mmm",
                                           "-3",
                                           "-3m",
                                           "6/m",
                                           "6/mmm",
                                           "m3",
                                           "m3m",
                                           "fiber"};

  public static String[] strainchoice = {"11", "22", "33", "23", "13", "12"};

  Sample actualsample = null;
//	int actuallayer = 0;

  int expansionDegree = 4;
  int sampleSymmetry = 0;

//	double acell[];
//	double astar[];

  int LGIndex = 0;
//	int PGIndex = 0;

  double[][][][][] coefficient = null;
  static double[][] legendreCoeff = null;
  static int[][] Cilm = null;
  static int[][] Jilm = null;
  public static int numberStrainParameters = 6; // remember should be equal to the one in StrainSphericalHarmonics

  public PopaBalzarHarmonicStrainModel(XRDcat aobj, String alabel) {
    super(aobj, alabel);
    initXRD();
    identifier = "WSODF Popa-Balzar unfinished";
    IDlabel = "WSODF Popa-Balzar unfinished";
    description = "select this to apply Harmonic model for WSODF of Popa-Balzar";
  }

  public PopaBalzarHarmonicStrainModel(XRDcat aobj) {
    this(aobj, "Harmonic method for WSODF");
  }

  public PopaBalzarHarmonicStrainModel(String[] labels) {
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

  public PopaBalzarHarmonicStrainModel() {
    identifier = "WSODF Popa-Balzar unfinished";
    IDlabel = "WSODF Popa-Balzar unfinished";
    description = "select this to apply Harmonic model for WSODF of Popa-Balzar";
  }

  public void initConstant() {
    Nstring = 2;
    Nstringloop = 0;
    Nparameter = 0;
    Nparameterloop = numberStrainParameters = 6;
    Nsubordinate = 0;
    Nsubordinateloop = 0;
  }

  public void initDictionary() {
    for (int i = 0; i < totsubordinateloop; i++)
      diclist[i] = diclistc[i];
    System.arraycopy(diclistcrm, 0, diclistRealMeaning, 0, totsubordinateloop);
    for (int i = 0; i < totsubordinateloop - totsubordinate; i++)
      classlist[i] = classlistc[i];
    for (int i = 0; i < totsubordinate - totparameterloop; i++)
      classlists[i] = classlistcs[i];
  }

  public void initParameters() {
    super.initParameters();

    setSampleSymmetry(0);
    setHarmonicExpansion(4);

    refreshComputation = true;
  }

  public String getSampleSymmetry() {
    return getString(0);
  }

  public int getSampleSymmetryValue() {

    String samplesym = getSampleSymmetry();

    for (int i = 0; i < symmetrychoice.length; i++) {
      if (samplesym.equals(symmetrychoice[i]))
        return i;
    }
    return 0;
  }

  public void setSampleSymmetry(int i) {
    setString(0, symmetrychoice[i]);
  }

  public void setSampleSymmetry(String value) {
    setString(0, value);
  }

  public String getHarmonicExpansion() {
    return getString(1);
  }

  public int getHarmonicExpansionValue() {
    return Integer.valueOf(getHarmonicExpansion()).intValue();
  }

  public void setHarmonicExpansion(int i) {
    setHarmonicExpansion(Integer.toString(i));
  }

  public void setHarmonicExpansion(String value) {
    setString(1, value);
  }

  public int getLaueGroupNumber() {
    return SpaceGroups.getLGNumberSiegfriedConv(getPhase());
  }

  public ListVector getHarmonicParameterList(int index) {
    return parameterloopField[index];
  }

  public int[] numberHarmonicParameters() {
    int[] nharm = new int[numberStrainParameters];
    for (int j = 0; j < numberStrainParameters; j++)
      nharm[j] = getHarmonicParameterList(j).size();
    return nharm;
  }

  public void setExpansionDegree(int value) {
    setHarmonicExpansion(value);
    if (expansionDegree != value) {
      expansionDegree = value;
      applySymmetryRules();
      refreshComputation = true;
    }
  }

  public void checkHarmonicParameters() {
    int[] numberHarmonics = getNumberHarmonics();
    int[] actualNumber = numberHarmonicParameters();

    isAbilitatetoRefresh = false;
    for (int j = 0; j < numberStrainParameters; j++)
      if (actualNumber[j] < numberHarmonics[j]) {
        for (int i = actualNumber[j]; i < numberHarmonics[j]; i++)
          addparameterloopField(j, new Parameter(this, getParameterString(j, i), 0,
                  ParameterPreferences.getDouble(getParameterString(j, i) + ".min", -0.1),
                  ParameterPreferences.getDouble(getParameterString(j, i) + ".max", 0.1)));
        refreshComputation = true;
      }

    for (int j = 0; j < numberStrainParameters; j++)
      if (actualNumber[j] > numberHarmonics[j]) {
        for (int i = actualNumber[j] - 1; i >= numberHarmonics[j]; i--)
          getHarmonicParameterList(j).removeItemAt(i);
        refreshComputation = true;
      }
    isAbilitatetoRefresh = true;
  }

  boolean[][][][][] validCoeff = null;

  public int[] getNumberHarmonics() {

    int[] index = new int[numberStrainParameters];

/*    for (int j = 0; j < numberStrainParameters; j++)
      index[j] = getNumberHarmonics(j);*/
    validCoeff = checkCoefficient(sampleSymmetry, LGIndex, expansionDegree);

    int expansionDegree2 = expansionDegree / 2 + 1;
    int expansionDegree1 = expansionDegree + 1;
    for (int i1 = 0; i1 < 4; i1++)
      for (int i2 = 0; i2 < numberStrainParameters; i2++)
        for (int i3 = 0; i3 < expansionDegree2; i3++)
          for (int i4 = 0; i4 < expansionDegree1; i4++)
            for (int i5 = 0; i5 < expansionDegree1; i5++) {
              if (validCoeff[i1][i2][i3][i4][i5])
                index[i2]++;
            }

    return index;
  }

  boolean[][][][][] checkCoefficient(int sampleSymmetry, int lgIndex, int expansionDegree) {

    int expansionDegree2 = expansionDegree / 2 + 1;
    int expansionDegree1 = expansionDegree + 1;
    int cubic_expansionDegree2 = 4 / 2 + 1;
    int cubic_expansionDegree1 = 4 + 1;
    boolean[][][][][] validCoeff =
            new boolean[4][numberStrainParameters][expansionDegree2][expansionDegree1][expansionDegree1];
    for (int i1 = 0; i1 < 4; i1++)
      for (int i2 = 0; i2 < numberStrainParameters; i2++)
        for (int i3 = 0; i3 < expansionDegree2; i3++)
          for (int i4 = 0; i4 < expansionDegree1; i4++)
            for (int i5 = 0; i5 < expansionDegree1; i5++)
              validCoeff[i1][i2][i3][i4][i5] = false;

    // ---------------------- Sample symmetry -----------------------------

    /*                                   0   "-1",
                                         1   "2/m",
                                         2   "2/mmm",
                                         3   "4/m",
                                         4   "4/mmm",
                                         5   "-3",
                                         6   "-3m",
                                         7   "6/m",
                                         8   "6/mmm",
                                         9   "m3",
                                         10  "m3m",
                                         11  "fiber" */
    int fold = 1;
    switch (sampleSymmetry) {
      case 1: // 2/m
      case 2: // 2/mmm
      case 9: // m-3
        fold = 2;
        break;
      case 5: // -3
      case 6: // -3m
        fold = 3;
        break;
      case 3: // 4/m
      case 4: // 4/mmm
      case 10: // m-3m
        fold = 4;
        break;
      case 7: // 6/m
      case 8: // 6/mmm
        fold = 6;
        break;
      case 11: // fiber
        fold = -1;
        break;
      case 0: // -1
      default:
        {
          fold = 1;
        }
    }
    switch (sampleSymmetry) {
      case 1: // 2/m
      case 5: // -3
      case 3: // 4/m
      case 7: // 6/m
        for (int i1 = 0; i1 < 4; i1++)
          for (int i2 = 0; i2 < numberStrainParameters; i2++)
            for (int i3 = 0; i3 < expansionDegree2; i3++)
              for (int i4 = 0; i4 < expansionDegree1; i4++)
                for (int i5 = 0; i5 < expansionDegree1; i5++) {
                  if (i5 == 0) {
                    if (i1 == 0 || i1 == 2)
                      validCoeff[i1][i2][i3][i4][i5] = true;
                  } else {
                    int rest = i5 / fold;
                    if (rest * fold == i5)
                      validCoeff[i1][i2][i3][i4][i5] = true;
                  }
                }
        break;
      case 2: // 2/mmm
      case 6: // -3m
      case 4: // 4/mmm
      case 8: // 6/mmm
      case 11: // fiber
        for (int i1 = 0; i1 < 4; i1++)
          for (int i2 = 0; i2 < numberStrainParameters; i2++)
            for (int i3 = 0; i3 < expansionDegree2; i3++)
              for (int i4 = 0; i4 < expansionDegree1; i4++)
                for (int i5 = 0; i5 < expansionDegree1; i5++) {
                  if (i5 == 0) {
                    if (i1 == 0 || i1 == 2)
                      validCoeff[i1][i2][i3][i4][i5] = true;
                  } else {
                    int rest = i5 / fold;
                    if (rest * Math.abs(fold) == i5) {
                      if (MoreMath.odd(i5)) {
                        if (i1 == 0 || i1 == 2)
                          validCoeff[i1][i2][i3][i4][i5] = true;
                      } else {
                        if (i1 == 1 || i1 == 3)
                          validCoeff[i1][i2][i3][i4][i5] = true;
                      }
                    }
                  }
                }
        break;
      case 9:
      case 10:
        for (int i1 = 0; i1 < 4; i1++)
          for (int i2 = 0; i2 < numberStrainParameters; i2++)
            for (int i3 = 0; i3 < expansionDegree2; i3++)
              for (int i4 = 0; i4 < expansionDegree1; i4++)
                for (int i5 = 0; i5 < expansionDegree1; i5++) {
                  if (i5 == 0) {
                    if (i1 == 0 || i1 == 2)
                      validCoeff[i1][i2][i3][i4][i5] = true;
                  } else {
                    int rest = i5 / fold;
                    if (rest * Math.abs(fold) == i5) {
                      if (MoreMath.odd(i5)) {
                        if (i1 == 0 || i1 == 2)
                          validCoeff[i1][i2][i3][i4][i5] = true;
                      } else {
                        if (i1 == 1 || i1 == 3)
                          validCoeff[i1][i2][i3][i4][i5] = true;
                      }
                    }
                  }
                }
        for (int i1 = 0; i1 < 4; i1 += 2)
          for (int i2 = 0; i2 < numberStrainParameters; i2++)
            for (int i4 = 0; i4 < cubic_expansionDegree1; i4++)
              validCoeff[i1][i2][2][i4][4] = false;
        for (int i1 = 0; i1 < 4; i1 += 2)
          for (int i2 = 0; i2 < numberStrainParameters; i2++)
            for (int i4 = 0; i4 < cubic_expansionDegree1; i4++) {
              validCoeff[i1][i2][2][i4][0] = false;
              if (sampleSymmetry == 9)
                validCoeff[i1][i2][2][i4][2] = false;
            }
        break;
      case 0: // -1
      default:
        {
          for (int i1 = 0; i1 < 4; i1++)
            for (int i2 = 0; i2 < numberStrainParameters; i2++)
              for (int i3 = 0; i3 < expansionDegree2; i3++)
                for (int i4 = 0; i4 < expansionDegree1; i4++)
                  for (int i5 = 0; i5 < expansionDegree1; i5++)
                    validCoeff[i1][i2][i3][i4][i5] = true;
        }
    }

    for (int i1 = 0; i1 < 4; i1++)
      for (int i2 = 0; i2 < numberStrainParameters; i2++)
        for (int i3 = 0; i3 < expansionDegree2; i3++)
          for (int i4 = 0; i4 < expansionDegree1; i4++)
            for (int i5 = 0; i5 < expansionDegree1; i5++)
              if (i4 > i3 * 2 || i5 > i3 * 2)
                validCoeff[i1][i2][i3][i4][i5] = false;

    for (int i1 = 2; i1 < 4; i1++)
      for (int i2 = 0; i2 < numberStrainParameters; i2++)
        for (int i3 = 0; i3 < expansionDegree2; i3++)
          for (int i5 = 0; i5 < expansionDegree1; i5++)
            validCoeff[i1][i2][i3][0][i5] = false;

    for (int i1 = 1; i1 < 4; i1+=2)
      for (int i2 = 0; i2 < numberStrainParameters; i2++)
        for (int i3 = 0; i3 < expansionDegree2; i3++)
          for (int i4 = 0; i4 < expansionDegree1; i4++)
            validCoeff[i1][i2][i3][i4][0] = false;


    // ---------------------- Crystal symmetry -----------------------------

    /*                                   0   "-1"    C1
                                         1   "2/m"   C2
                                         2   "2/mmm" D2
                                         3   "4/m"   C4
                                         4   "4/mmm" D4
                                         5   "-3"    C3
                                         6   "-3m"   D3
                                         7   "6/m"   C6
                                         8   "6/mmm" D6
                                         9   "m3"    T
                                         10  "m3m"   O
                                    */

    switch (lgIndex) {
      case 0: // C1
        break;
      case 1: // C2
        for (int i1 = 0; i1 < 4; i1++)
          for (int i2 = 0; i2 < numberStrainParameters; i2++)
            for (int i3 = 0; i3 < expansionDegree2; i3++)
              for (int i4 = 0; i4 < expansionDegree1; i4++)
                for (int i5 = 0; i5 < expansionDegree1; i5++) {
                  switch (i2) {
                    case 0:
                    case 1:
                    case 2:
                    case 5:
                      if (MoreMath.odd(i4)) {
                        validCoeff[i1][i2][i3][i4][i5] = false;
                      }
                      break;
                    case 3:
                    case 4:
                      if (!MoreMath.odd(i4)) {
                        validCoeff[i1][i2][i3][i4][i5] = false;
                      }
                      break;
                    default:
                      {
                      }
                  }
                }
        break;
      case 3: // C4
        for (int i1 = 0; i1 < 4; i1++)
          for (int i2 = 0; i2 < numberStrainParameters; i2++)
            for (int i3 = 0; i3 < expansionDegree2; i3++)
              for (int i4 = 0; i4 < expansionDegree1; i4++)
                for (int i5 = 0; i5 < expansionDegree1; i5++) {
                  switch (i2) {
                    case 0:
                      if (MoreMath.odd(i4)) {
                        validCoeff[i1][i2][i3][i4][i5] = false;
                      }
                      break;
                    case 1:
                      validCoeff[i1][i2][i3][i4][i5] = false;
                      break;
                    case 2:
                      if (MoreMath.odd(i4 / 2)) {
                        validCoeff[i1][i2][i3][i4][i5] = false;
                      }
                      break;
                    case 3:
                      if (!MoreMath.odd(i4)) {
                        validCoeff[i1][i2][i3][i4][i5] = false;
                      }
                      break;
                    case 4:
                      validCoeff[i1][i2][i3][i4][i5] = false;
                      break;
                    case 5:
                      if (MoreMath.odd((i4 + 2) / 2)) {
                        validCoeff[i1][i2][i3][i4][i5] = false;
                      }
                      break;
                    default:
                      {
                      }
                  }
                }
        break;
      case 5: // C3
        break;
      case 7: // C6
        break;
      case 2: // D2
        for (int i1 = 0; i1 < 4; i1++)
          for (int i2 = 0; i2 < numberStrainParameters; i2++)
            for (int i3 = 0; i3 < expansionDegree2; i3++)
              for (int i4 = 0; i4 < expansionDegree1; i4++)
                for (int i5 = 0; i5 < expansionDegree1; i5++) {
                  switch (i2) {
                    case 0:
                    case 1:
                    case 2:
                      if (MoreMath.odd(i4) || i1 > 1) {
                        validCoeff[i1][i2][i3][i4][i5] = false;
                      }
                      break;
                    case 3:
                      if (!MoreMath.odd(i4) || i1 < 2) {
                        validCoeff[i1][i2][i3][i4][i5] = false;
                      }
                      break;
                    case 4:
                      if (!MoreMath.odd(i4) || i1 > 1) {
                        validCoeff[i1][i2][i3][i4][i5] = false;
                      }
                      break;
                    case 5:
                      if (MoreMath.odd(i4) || i1 < 2) {
                        validCoeff[i1][i2][i3][i4][i5] = false;
                      }
                      break;
                    default:
                      {
                      }
                  }
                }
        break;
      case 4: // D4
        for (int i1 = 0; i1 < 4; i1++)
          for (int i2 = 0; i2 < numberStrainParameters; i2++)
            for (int i3 = 0; i3 < expansionDegree2; i3++)
              for (int i4 = 0; i4 < expansionDegree1; i4++)
                for (int i5 = 0; i5 < expansionDegree1; i5++) {
                  switch (i2) {
                    case 0:
                      if (i1 > 1 || MoreMath.odd(i4)) {
                        validCoeff[i1][i2][i3][i4][i5] = false;
                      }
                      break;
                    case 1:
                      validCoeff[i1][i2][i3][i4][i5] = false;
                      break;
                    case 2:
                      if (i1 > 1 || MoreMath.odd(i4 / 2)) {
                        validCoeff[i1][i2][i3][i4][i5] = false;
                      }
                      break;
                    case 3:
                      if (i1 < 2 || !MoreMath.odd(i4)) {
                        validCoeff[i1][i2][i3][i4][i5] = false;
                      }
                      break;
                    case 4:
                      validCoeff[i1][i2][i3][i4][i5] = false;
                      break;
                    case 5:
                      if (i1 < 2 || MoreMath.odd((i4 + 2) / 2)) {
                        validCoeff[i1][i2][i3][i4][i5] = false;
                      }
                      break;
                    default:
                      {
                      }
                  }
                }
        break;
      case 6: // D3
        break;
      case 8: // D6
        break;
      case 9: // T
        // as the orthorhombic
        for (int i1 = 0; i1 < 4; i1++)
          for (int i2 = 0; i2 < numberStrainParameters; i2++)
            for (int i3 = 0; i3 < expansionDegree2; i3++)
              for (int i4 = 0; i4 < expansionDegree1; i4++)
                for (int i5 = 0; i5 < expansionDegree1; i5++) {
                  if (i3 > 2)
                    validCoeff[i1][i2][i3][i4][i5] = false;
                  else
                    switch (i2) {
                      case 0:
                      case 1:
                      case 2:
                        if (MoreMath.odd(i4) || i1 > 1) {
                          validCoeff[i1][i2][i3][i4][i5] = false;
                        }
                        break;
                      case 3:
                        if (!MoreMath.odd(i4) || i1 < 2) {
                          validCoeff[i1][i2][i3][i4][i5] = false;
                        }
                        break;
                      case 4:
                        if (!MoreMath.odd(i4) || i1 > 1) {
                          validCoeff[i1][i2][i3][i4][i5] = false;
                        }
                        break;
                      case 5:
                        if (MoreMath.odd(i4) || i1 < 2) {
                          validCoeff[i1][i2][i3][i4][i5] = false;
                        }
                        break;
                      default:
                        {
                        }
                    }
                }
        // now for the T, only up to l = 4
        for (int i1 = 0; i1 < 2; i1++)
          for (int i5 = 0; i5 < expansionDegree1; i5++) {
            int i3 = 0;
            int i4 = 0;
            validCoeff[i1][2][i3][i4][i5] = false;
            validCoeff[i1][1][i3][i4][i5] = false;
            i3 = 1;
            i4 = 2;
            validCoeff[i1][0][i3][i4][i5] = false;
            validCoeff[i1][1][i3][i4][i5] = false;
            validCoeff[i1][2][i3][i4][i5] = false;
            i3 = 2;
            i4 = 4;
            validCoeff[i1][0][i3][i4][i5] = false;
            validCoeff[i1][1][i3][i4][i5] = false;
            i4 = 3;
            validCoeff[i1][4][i3][i4][i5] = false;
          }
        for (int i1 = 2; i1 < 4; i1++)
          for (int i5 = 0; i5 < expansionDegree1; i5++) {
            int i3 = 1;
            int i4 = 2;
            validCoeff[i1][5][i3][i4][i5] = false;
            i3 = 2;
            i4 = 3;
            validCoeff[i1][3][i3][i4][i5] = false;
            i4 = 2;
            validCoeff[i1][5][i3][i4][i5] = false;
            i4 = 4;
            validCoeff[i1][5][i3][i4][i5] = false;
          }
        break;
      case 10: // O
        // for the tetragonal case
        for (int i1 = 0; i1 < 4; i1++)
          for (int i2 = 0; i2 < numberStrainParameters; i2++)
            for (int i3 = 0; i3 < expansionDegree2; i3++)
              for (int i4 = 0; i4 < expansionDegree1; i4++)
                for (int i5 = 0; i5 < expansionDegree1; i5++) {
                  if (i3 > 2) // only up to l = 4
                    validCoeff[i1][i2][i3][i4][i5] = false;
                  else
                    switch (i2) {
                      case 0:
                        if (i1 > 1 || MoreMath.odd(i4)) {
                          validCoeff[i1][i2][i3][i4][i5] = false;
                        }
                        break;
                      case 1:
                        validCoeff[i1][i2][i3][i4][i5] = false;
                        break;
                      case 2:
                        if (i1 > 1 || MoreMath.odd(i4 / 2)) {
                          validCoeff[i1][i2][i3][i4][i5] = false;
                        }
                        break;
                      case 3:
                        if (i1 < 2 || !MoreMath.odd(i4)) {
                          validCoeff[i1][i2][i3][i4][i5] = false;
                        }
                        break;
                      case 4:
                        validCoeff[i1][i2][i3][i4][i5] = false;
                        break;
                      case 5:
                        if (i1 < 2 || MoreMath.odd((i4 + 2) / 2)) {
                          validCoeff[i1][i2][i3][i4][i5] = false;
                        }
                        break;
                      default:
                        {
                        }
                    }
                }
        // now the O group, only up to l = 4
        for (int i1 = 0; i1 < 2; i1++)
          for (int i5 = 0; i5 < expansionDegree1; i5++) {
            int i3 = 0;
            int i4 = 0;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              validCoeff[i1][2][i3][i4][i5] = false;
            i3 = 1;
            i4 = 2;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              validCoeff[i1][0][i3][i4][i5] = false;
            i3 = 2;
            i4 = 4;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              validCoeff[i1][0][i3][i4][i5] = false;
          }
        for (int i1 = 2; i1 < 4; i1++)
          for (int i5 = 0; i5 < expansionDegree1; i5++) {
            int i3 = 1;
            int i4 = 2;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              validCoeff[i1][5][i3][i4][i5] = false;
            i3 = 2;
            i4 = 3;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              validCoeff[i1][3][i3][i4][i5] = false;
            i4 = 2;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              validCoeff[i1][5][i3][i4][i5] = false;
          }
        break;
      default:
        {
        }
    }


    return validCoeff;
  }

/*  public int getNumberHarmonics(int strainIndex) {

    int index = 0;

    for (int l = 0; l <= expansionDegree; l += 2) {
      index += StrainSphericalHarmonics.getN(LGIndex, l, strainIndex) *
              StrainSphericalHarmonics.getN(sampleSymmetry, l);
    }

    return index;
  }*/

  public int getLGnumber() {
    return ((Phase) getPhase()).getLaueGroup();
  }

  public int getPGnumber() {
    return ((Phase) getPhase()).getPointGroup();
  }

  public void applySymmetryRules() {
    LGIndex = SpaceGroups.getLGNumber(getPhase());
    sampleSymmetry = getSampleSymmetryValue();
    expansionDegree = getHarmonicExpansionValue();
    MoreMath.initFactorial(expansionDegree + 2);
    initLegendre(expansionDegree + 1);
    checkHarmonicParameters();
    refreshCoefficients();
  }

  public void refreshCoefficients() {
    double[][] coefficients = getParameterLoopVector();
    int expansionDegree2 = expansionDegree / 2 + 1;
    int expansionDegree1 = expansionDegree + 1;
    int[] index = new int[numberStrainParameters];
    coefficient = new double[4][numberStrainParameters][expansionDegree2][expansionDegree1][expansionDegree1];
    for (int i1 = 0; i1 < 4; i1++)
      for (int i2 = 0; i2 < numberStrainParameters; i2++)
        for (int i3 = 0; i3 < expansionDegree2; i3++)
          for (int i4 = 0; i4 < expansionDegree1; i4++)
            for (int i5 = 0; i5 < expansionDegree1; i5++) {
              if (validCoeff[i1][i2][i3][i4][i5]) {
                coefficient[i1][i2][i3][i4][i5] = coefficients[i2][index[i2]];
                index[i2]++;
              }
            }
    switch (LGIndex) {
      case 0: // C1
        break;
      case 1: // C2
        break;
      case 3: // C4
        for (int i1 = 0; i1 < 4; i1++)
          for (int i2 = 0; i2 < numberStrainParameters; i2++)
            for (int i3 = 0; i3 < expansionDegree2; i3++)
              for (int i4 = 0; i4 < expansionDegree1; i4++)
                for (int i5 = 0; i5 < expansionDegree1; i5++) {
                  switch (i2) {
                    case 1:
                      if (i4 == 0)
                        coefficient[i1][i2][i3][i4][i5] = coefficient[i1][0][i3][i4][i5];
                      else
                        coefficient[i1][i2][i3][i4][i5] = coefficient[i1][0][i3][i4][i5] * MoreMath.powint(-1, i4/2);
                      break;
                    case 4:
                      if (i1 < 2)
                        coefficient[i1][i2][i3][i4][i5] = coefficient[i1+2][3][i3][i4][i5] *
                                                          MoreMath.powint(-1, (i4+1)/2 - 1);
                      else
                        coefficient[i1][i2][i3][i4][i5] = coefficient[i1-2][3][i3][i4][i5] *
                                                          MoreMath.powint(-1, (i4+1)/2);
                      break;
                    default:
                      {
                      }
                  }
                }
        break;
      case 5: // C3
        break;
      case 7: // C6
        break;
      case 2: // D2
        break;
      case 4: // D4
        for (int i1 = 0; i1 < 2; i1++)
          for (int i2 = 0; i2 < numberStrainParameters; i2++)
            for (int i3 = 0; i3 < expansionDegree2; i3++)
              for (int i4 = 0; i4 < expansionDegree1; i4++)
                for (int i5 = 0; i5 < expansionDegree1; i5++) {
                  switch (i2) {
                    case 1:
                      coefficient[i1][i2][i3][i4][i5] = coefficient[i1][0][i3][i4][i5] * MoreMath.powint(-1,i4/2);
                      break;
                    case 4:
                      coefficient[i1][i2][i3][i4][i5] = coefficient[i1+2][3][i3][i4][i5] *
                                                          MoreMath.powint(-1, (i4+1)/2 - 1);
                      break;
                    default: {}
                  }
                }
        break;
      case 6: // D3
        break;
      case 8: // D6
        break;
      case 9: // T
        // now for the T, only up to l = 4
        for (int i1 = 0; i1 < 2; i1++)
          for (int i5 = 0; i5 < expansionDegree1; i5++) {
            int i3 = 0;
            int i4 = 0;
            coefficient[i1][2][i3][i4][i5] = coefficient[i1][0][i3][i4][i5];
            coefficient[i1][1][i3][i4][i5] = coefficient[i1][0][i3][i4][i5];
            i3 = 1;
            i4 = 2;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              coefficient[i1][0][i3][i4][i5] = c23 * (coefficient[i1][0][i3][0][i5] +
                                             2.0 * coefficient[i1][2][i3][0][i5]);
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              coefficient[i1][1][i3][i4][i5] = -c23 * (coefficient[i1][1][i3][0][i5] +
                                             2.0 * coefficient[i1][2][i3][0][i5]);
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              coefficient[i1][2][i3][i4][i5] = -c23 * (coefficient[i1][0][i3][0][i5] -
                                             coefficient[i1][1][i3][0][i5]) + 2.0 *
                                             (coefficient[i1+2][3][i3][1][i5] -
                                             coefficient[i1][4][i3][1][i5]);
            i3 = 2;
            i4 = 4;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              coefficient[i1][0][i3][i4][i5] = c335 * coefficient[i1][0][i3][0][i5] +
                                             c835 * coefficient[i1][2][i3][0][i5] +
                                             c27 * coefficient[i1][0][i3][2][i5];;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              coefficient[i1][1][i3][i4][i5] = c335 * coefficient[i1][1][i3][0][i5] +
                                             c835 * coefficient[i1][2][i3][0][i5] -
                                             c27 * coefficient[i1][1][i3][2][i5];;
            i4 = 3;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              coefficient[i1][4][i3][i4][i5] = c2d35 * (3.0 * coefficient[i1][0][i3][0][i5] +
                                             coefficient[i1][1][i3][0][i5]) -
                                             c2735 * coefficient[i1][2][i3][0][i5] +
                                             c0514 * (6.0 * coefficient[i1][0][i3][2][i5] +
                                             5.0 * coefficient[i1][2][i3][2][i5]) -
                                             c7 * (4.0 * coefficient[i1+2][3][i3][1][i5] +
                                             3.0 * coefficient[i1][4][i3][1][i5]) +
                                             c025 * coefficient[i1][2][i3][4][i5];
          }
        for (int i1 = 2; i1 < 4; i1++)
          for (int i5 = 0; i5 < expansionDegree1; i5++) {
            int i3 = 1;
            int i4 = 2;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              coefficient[i1][5][i3][i4][i5] = c32 * 0.5 * (coefficient[i1-2][0][i3][0][i5] +
                                             coefficient[i1-2][1][i3][0][i5] +
                                             coefficient[i1-2][2][i3][0][i5]) +
                                             0.5 * (coefficient[i1][3][i3][1][i5] +
                                             coefficient[i1][4][i3][1][i5]);
            i3 = 2;
            i4 = 3;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              coefficient[i1][3][i3][i4][i5] = c2d35 * (coefficient[i1-2][0][i3][0][i5] +
                                             3.0 * coefficient[i1-2][1][i3][0][i5]) +
                                             c2735 * coefficient[i1-2][2][i3][0][i5] +
                                             c0514 * (6.0 * coefficient[i1-2][1][i3][2][i5] +
                                             5.0 * coefficient[i1-2][2][i3][2][i5]) +
                                             c7 * (3.0 * coefficient[i1][3][i3][1][i5] +
                                             4.0 * coefficient[i1-2][4][i3][1][i5]) -
                                             c025 * coefficient[i1-2][2][i3][4][i5];
            i4 = 2;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              coefficient[i1][5][i3][i4][i5] = c25 * (coefficient[i1-2][0][i3][0][i5] +
                                             coefficient[i1-2][1][i3][0][i5] -
                                             2.0 * coefficient[i1-2][2][i3][0][i5]) -
                                             0.5 * (coefficient[i1-2][0][i3][2][i5] -
                                             coefficient[i1-2][1][i3][2][i5]) -
                                             Constants.sqrt2 * (coefficient[i1][3][i3][1][i5] +
                                             coefficient[i1-2][4][i3][1][i5]);
            i4 = 4;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              coefficient[i1][5][i3][i4][i5] = c235 * (coefficient[i1-2][0][i3][0][i5] -
                                             coefficient[i1-2][1][i3][0][i5]) -
                                             (coefficient[i1-2][0][i3][2][i5] +
                                             coefficient[i1-2][1][i3][2][i5] +
                                             1.5 * coefficient[i1-2][2][i3][2][i5]) / c7 -
                                             cs27 * (coefficient[i1][3][i3][1][i5] -
                                             coefficient[i1-2][4][i3][1][i5]);
          }
        break;
      case 10: // O
        // for the tetragonal case
        for (int i1 = 0; i1 < 2; i1++)
          for (int i2 = 0; i2 < numberStrainParameters; i2++)
            for (int i3 = 0; i3 < expansionDegree2; i3++)
              for (int i4 = 0; i4 < expansionDegree1; i4++)
                for (int i5 = 0; i5 < expansionDegree1; i5++) {
                  switch (i2) {
                    case 1:
                      coefficient[i1][i2][i3][i4][i5] = coefficient[i1][0][i3][i4][i5] * MoreMath.powint(-1,i4/2);
                      break;
                    case 4:
                      coefficient[i1][i2][i3][i4][i5] = coefficient[i1+2][3][i3][i4][i5] *
                                                          MoreMath.powint(-1, (i4+1)/2 - 1);
                      break;
                    default: {}
                  }
                }
        // now the O group, only up to l = 4
        for (int i1 = 0; i1 < 2; i1++)
          for (int i5 = 0; i5 < expansionDegree1; i5++) {
            int i3 = 0;
            int i4 = 0;
            coefficient[i1][2][i3][i4][i5] = coefficient[i1][0][i3][i4][i5];
            i3 = 1;
            i4 = 2;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              coefficient[i1][0][i3][i4][i5] = c23 * (coefficient[i1][0][i3][0][i5] +
                                             2.0 * coefficient[i1][2][i3][0][i5]);
            i3 = 2;
            i4 = 4;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              coefficient[i1][0][i3][i4][i5] = c335 * coefficient[i1][0][i3][0][i5] +
                                             c835 * coefficient[i1][2][i3][0][i5] +
                                             c27 * coefficient[i1][0][i3][2][i5];
          }
        for (int i1 = 2; i1 < 4; i1++)
          for (int i5 = 0; i5 < expansionDegree1; i5++) {
            int i3 = 1;
            int i4 = 2;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              coefficient[i1][5][i3][i4][i5] = c32 * (coefficient[i1-2][0][i3][0][i5] +
                                             0.5 * coefficient[i1-2][2][i3][0][i5]) +
                                             coefficient[i1][3][i3][1][i5];
            i3 = 2;
            i4 = 3;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              coefficient[i1][3][i3][i4][i5] = c8d35 * coefficient[i1-2][0][i3][0][i5] +
                                             c2735 * coefficient[i1-2][2][i3][0][i5] +
                                             c314 * coefficient[i1-2][0][i3][2][i5] +
                                             c7 * coefficient[i1][3][i3][1][i5] +
                                             c025 * coefficient[i1-2][2][i3][4][i5];
            i4 = 2;
            if (i4 < expansionDegree1 && i3 < expansionDegree2)
              coefficient[i1][5][i3][i4][i5] = c225 * (coefficient[i1-2][0][i3][0][i5] -
                                             coefficient[i1-2][2][i3][0][i5]) -
                                             coefficient[i1-2][0][i3][2][i5] -
                                             c8 * coefficient[i1][3][i3][1][i5];
          }
        break;
      default:
        {
        }
    }

    for (int i2 = 0; i2 < numberStrainParameters; i2++)
        for (int i3 = 0; i3 < expansionDegree2; i3++)
          for (int i4 = 0; i4 <= i3 * 2; i4++)
            for (int i5 = 0; i5 <= i3 * 2; i5++) {
              Misc.println("a "+(i2+1)+","+(i3*2)+","+i4+","+i5+":"+coefficient[0][i2][i3][i4][i5]);
              Misc.println("b "+(i2+1)+","+(i3*2)+","+i4+","+i5+":"+coefficient[1][i2][i3][i4][i5]);
              Misc.println("g "+(i2+1)+","+(i3*2)+","+i4+","+i5+":"+coefficient[2][i2][i3][i4][i5]);
              Misc.println("d "+(i2+1)+","+(i3*2)+","+i4+","+i5+":"+coefficient[3][i2][i3][i4][i5]);
            }
  }

  static double c23 = Math.sqrt(2.0 / 3.0);
  static double c32 = Math.sqrt(3.0 / 2.0);
  static double c335 = -3.0 * Math.sqrt(2.0 / 35.0);
  static double c835 = 8.0 * Math.sqrt(2.0 / 35.0);
  static double c235 = -Math.sqrt(2.0 / 35.0);
  static double c27 = 2.0 / Math.sqrt(7.0);
  static double c8d35 = -8.0 / Math.sqrt(35.0);
  static double c2d35 = -2.0 * Math.sqrt(2.0 / 35.0);
  static double c2735 = 27.0 / 4.0 / Math.sqrt(35.0);
  static double c314 = 3.0 / Math.sqrt(14.0);
  static double c0514 = -0.5 / Math.sqrt(14.0);
  static double c7 = -Math.sqrt(7.0);
  static double c025 = 0.25 / Math.sqrt(2.0);
  static double c225 = 2.0 * Math.sqrt(2.0 / 5.0);
  static double c25 = Math.sqrt(2.0 / 5.0);
  static double c8 = -Math.sqrt(8.0);
  static double cs27 = -Math.sqrt(2.0 / 7.0);

  public double[][] getParameterLoopVector() {
    Vector[] coeffVect = new Vector[numberStrainParameters];
    for (int i = 0; i < numberStrainParameters; i++)
      coeffVect[i] = parameterloopField[i];
    return StrainSphericalHarmonics.getParameterMatrix(coeffVect);
  }

  public void computeStrain(Sample asample) {

    Phase aphase = getPhase();
    computeStrain(aphase, asample);

  }

  public void computeStrain(Phase aphase, Sample asample) {

    if (refreshComputation) {
      FilePar aparFile = (FilePar) getFilePar();

//		double[] cdsc = Uwimvuo.lattice(aphase);

      applySymmetryRules();
      aphase.sghklcompute(false);
      refreshComputation = false;
      int hkln = aphase.gethklNumber();
      for (int j = 0; j < hkln; j++) {
        Reflection refl = (Reflection) aphase.reflectionv.elementAt(j);
        double position = 0.0;
        double dspace = aphase.getDspacing(j);

        int datafile = 0;
        for (int i = 0; i < asample.activeDatasetsNumber(); i++) {
          DataFileSet adataset = asample.getActiveDataSet(i);
//					Instrument ainstrument = adataset.getInstrument();
//					Radiation rad = ainstrument.getRadiationType().getRadiation(0);

          int datafilenumber = adataset.activedatafilesnumber();
          for (int i1 = 0; i1 < datafilenumber; i1++) {
            DiffrDataFile adatafile = adataset.getActiveDataFile(i1);
            datafile = adatafile.getIndex();
            if (adatafile.dspacingbase)
              position = dspace;
            else
              position = adataset.computeposition(dspace, 0);
            if (position != 180) {
              position = adatafile.getCorrectedPosition(asample, position);
              float strain_angles[] = adatafile.getTextureAngles((float) position);
              double textF = computeStrain(refl.phi[0], refl.beta[0],
                      strain_angles[0] * Constants.DEGTOPI,
                      strain_angles[1] * Constants.DEGTOPI);
              refl.setStrain(datafile, textF);
              refl.setExpStrain(datafile, textF);
            }

          }
        }
      }
    }

//    computeMacroStrain();

  }

  public double computeStrain(double phi, double beta, double psi, double gamma) {
    // Angles must be in radiants
    // phi and beta are the polar and azimuthal angles for the crystal setting
    // psi and gamma for the sample
    // see Popa, J. Appl. Cryst. 25, 611, 1992.

//    double[] poleIntensity = new double[numberStrainParameters];

    double sinphi = Math.sin(phi);
    double cosphi = Math.cos(phi);
    double sinpsi = Math.sin(psi);
    double cospsi = Math.cos(psi);
    double A1 = Math.cos(beta) * sinphi;
    double A2 = Math.sin(beta) * sinphi;
    double A3 = Math.cos(phi);
    double[] Erho = new double[6];
    Erho[0] = A1 * A1;
    Erho[1] = A2 * A2;
    Erho[2] = A3 * A3;
    Erho[3] = 2.0 * A2 * A3;
    Erho[4] = 2.0 * A1 * A3;
    Erho[5] = 2.0 * A1 * A2;

    double strain33 = 0.0;
    double[] thy = new double[numberStrainParameters];
    double[] Am = new double[numberStrainParameters];
    double[] Bm = new double[numberStrainParameters];
    double ngamma, cosngamma, sinngamma, mbeta, cosmbeta, sinmbeta;

    for (int l = 0; l <= expansionDegree; l += 2) {
      ngamma = 0.0;
      double[] legr = new double[l + 1];
      for (int n = 0; n <= l; n++)
        legr[n] = getLegendre(l, n, cospsi, sinpsi);
      for (int i = 0; i < numberStrainParameters; i++) {
        Am[i] = coefficient[0][i][l/2][0][0] * legr[0];
      }
      for (int n = 1; n <= l; n++) {
        ngamma += gamma;
        cosngamma = Math.cos(ngamma);
        sinngamma = Math.sin(ngamma);
        for (int i = 0; i < numberStrainParameters; i++) {
          Am[i] += (coefficient[0][i][l/2][0][n] * cosngamma + coefficient[1][i][l/2][0][n] * sinngamma) *
                  legr[n];
        }
      }
      mbeta = 0.0;
      double legrm = getLegendre(l, 0, cosphi, sinphi);
      for (int i = 0; i < numberStrainParameters; i++) {
        thy[i] = Am[i] * legrm;
      }
      for (int m = 1; m <= l; m++) {
        ngamma = 0.0;
        for (int i = 0; i < numberStrainParameters; i++) {
          Am[i] = coefficient[0][i][l/2][m][0] * legr[0];
          Bm[i] = coefficient[2][i][l/2][m][0] * legr[0];
        }
        //       int nl2 = StrainSphericalHarmonics.getN(sampleSymmetry, l);
        for (int n = 1; n <= l; n++) {
          ngamma += gamma;
          cosngamma = Math.cos(ngamma);
          sinngamma = Math.sin(ngamma);
          for (int i = 0; i < numberStrainParameters; i++) {
            Am[i] += (coefficient[0][i][l/2][m][n] * cosngamma + coefficient[1][i][l/2][m][n] * sinngamma) * legr[n];
            Bm[i] += (coefficient[2][i][l/2][m][n] * cosngamma + coefficient[3][i][l/2][m][n] * sinngamma) * legr[n];
          }
        }
        mbeta += beta;
        cosmbeta = Math.cos(mbeta);
        sinmbeta = Math.sin(mbeta);
        legrm = getLegendre(l, m, cosphi, sinphi);
        for (int i = 0; i < numberStrainParameters; i++) {
          thy[i] += (Am[i] * cosmbeta + Bm[i] * sinmbeta) * legrm;
        }
      }
      strain33 += 2.0 / (2.0 * l + 1.0) * getStrain33(thy, Erho);
    }
    return strain33;
  }

  public static void initLegendre(int maxExpansion) {
    if (Cilm != null && Cilm[0].length >= maxExpansion)
      return;
    legendreCoeff = new double[maxExpansion / 2 + 1][maxExpansion];
    for (int l = 0; l < maxExpansion; l += 2) {
      for (int m = 0; m <= l; m++) {
        legendreCoeff[l / 2][m] = Math.sqrt(MoreMath.factorial(l, m)) *
                Math.sqrt((2.0 * l + 1.0) / 2.0) *
                MoreMath.pow_ii(l - m) / (MoreMath.powint(2, l) * MoreMath.fact(l));
      }
    }
    Cilm = new int[maxExpansion][maxExpansion];
    Jilm = new int[maxExpansion][maxExpansion];
    for (int i = 0; i < maxExpansion; i++) {
      int diag = i;
      Jilm[i][i] = diag;
      int j = i;
      while (diag > 1) {
        diag -= 2;
        Jilm[i][--j] = diag;
      }
    }
    Cilm[0][0] = 1;
    Cilm[1][1] = -2;
    for (int n = 2; n < maxExpansion; n++) {
//      int diag = MoreMath.powint(-2, n);
      for (int j = (n + 1) / 2; j <= n; j++) {
        if (j == (int) (n + 1) / 2) {
          if (MoreMath.odd(n))
            Cilm[n][j] = 2 * Cilm[n - 1][j] - 2 * Cilm[n - 1][j - 1];
          else
            Cilm[n][j] = Cilm[n - 1][j];
        } else if (j == n) {
          Cilm[n][j] = -2 * Cilm[n - 1][j - 1];
        } else {
          Cilm[n][j] = Cilm[n - 1][j] * Jilm[n - 1][j] - 2 * Cilm[n - 1][j - 1];
        }
      }
    }
    for (int i = 0; i < maxExpansion; i++) {
      for (int j = 0; j < maxExpansion; j++) {
        Misc.println(i + " " + j + " : " + Cilm[i][j]);
      }
    }
    for (int i = 0; i < maxExpansion; i++) {
      for (int j = 0; j < maxExpansion; j++) {
        Misc.println(i + " " + j + " : " + Jilm[i][j]);
      }
    }
  }

  double getLegendre(int l, int m, double cosphi, double sinphi) {
    double Plm = 0.0;
    int n = l - m;
    for (int j = (n + 1) / 2; j <= n; j++)
      Plm += Cilm[n][j] * MoreMath.factorial(l, l - j) * MoreMath.pow(sinphi, l - j) *
              MoreMath.pow(cosphi, Jilm[n][j]);
    Plm *= Math.pow(sinphi, -0.5 * m) * legendreCoeff[l / 2][m];
    return Plm;
  }

  public double getStrain33(double[] Thy, double[] Erho) {
    double strain33 = 0.0;
    for (int i = 0; i < 6; i++) {
      strain33 += (Erho[i] * Thy[i]);
    }
    return strain33;
  }

  public double[] getODF(double alpha, double beta, double gamma) {
    int k = 0;

    alpha = Constants.PI - alpha;
    gamma = Constants.PI - gamma;

    double[] wsodf = new double[numberStrainParameters];

    /*
    for (int i = 0; i < numberStrainParameters; i++)
      wsodf[i] = coefficient[i][k];
    k++;
    for (int l = 2; l <= expansionDegree; l += 2) {
      int nl2 = StrainSphericalHarmonics.getN(sampleSymmetry, l);
      for (int n = 1; n <= nl2; n++) {
        int ml2 = StrainSphericalHarmonics.getN(LGIndex, l);
        for (int m = 1; m <= ml2; m++) {
          for (int i = 0; i < numberStrainParameters; i++)
            wsodf[i] += coefficient[i][k] * StrainSphericalHarmonics.getDSphericalHarmonic(
                    LGIndex, sampleSymmetry, l, m, n, gamma, beta, alpha, i);
          k++;
        }
      }
    } */
    return wsodf;
  }

  public static double computeStrain(double[][][] odfl, double[] cdsc, double strain_angles[],
                                     double[] sctf, double fhir, int inv, double phoninp,
                                     double res) {

    double pfValue = 0.0;

    return pfValue;
  }

  public double[][] getPoleFigureGrid(Reflection refl, int numberofPoints, double maxAngle) {

    double[][] PFreconstructed = new double[numberofPoints][numberofPoints];

    double strain_angles[] = new double[2];

    double x , y, r;
    double dxy = 2.0 * maxAngle / (numberofPoints - 1);

//		Phase aphase = (Phase) refl.getParent();
    applySymmetryRules();
//		aphase.sghklcompute(false);

    for (int i = 0; i < numberofPoints; i++)
      for (int j = 0; j < numberofPoints; j++) {
        x = j * dxy - maxAngle;
        y = i * dxy - maxAngle;
        r = Math.sqrt(x * x + y * y);
        if (r == 0.0) {
          strain_angles[0] = 0.0;
          strain_angles[1] = 0.0;
          PFreconstructed[i][j] = computeStrain(refl.phi[0], refl.beta[0],
                  strain_angles[0] * Constants.DEGTOPI,
                  strain_angles[1] * Constants.DEGTOPI);
        } else if (r <= maxAngle) {
          double phaseAng = Math.atan2(x, y);
          if (phaseAng < 0.0)
            phaseAng += Constants.PI2;
          strain_angles[0] = 2.0 * Math.asin(r / Constants.sqrt2) * Constants.PITODEG;
          if (strain_angles[0] < 0.0) {
            strain_angles[0] = -strain_angles[0];
            phaseAng += Constants.PI;
            while (phaseAng >= Constants.PI2)
              phaseAng -= Constants.PI2;
          }
          strain_angles[1] = phaseAng * Constants.PITODEG;
//					Misc.println(Double.toXRDcatString(strain_angles[0]) + " " + Double.toXRDcatString(strain_angles[1]));

          PFreconstructed[i][j] = computeStrain(refl.phi[0], refl.beta[0],
                  strain_angles[0] * Constants.DEGTOPI,
                  strain_angles[1] * Constants.DEGTOPI);
        } else
          PFreconstructed[i][j] = Double.NaN;
      }
    return PFreconstructed;
  }

  static double[][][] wijk = null;

  static {
    wijk = new double[6][6][26];

/*    wijk[0][0][0] = 2./3.;
    wijk[1][0][0] = 2./3.;
    wijk[2][0][0] = 2./3.;
    wijk[3][0][0] = 2./3.;
    wijk[4][0][0] = 2./3.;
    wijk[5][0][0] = 2./3.;
    wijk[0][2][0] = 2./3.;
    wijk[1][2][0] = 2./3.;
    wijk[2][2][0] = 2./3.;
    wijk[3][2][0] = 2./3.;
    wijk[4][2][0] = 2./3.;
    wijk[5][2][0] = 2./3.;
    wijk[0][0][0] = 2./3.;
    wijk[0][1][0] = 2./3.;
    wijk[0][2][0] = 2./3.;
    wijk[0][3][0] = 2./3.;
    wijk[0][4][0] = 2./3.;
    wijk[0][5][0] = 2./3.;
    wijk[2][0][0] = 2./3.;
    wijk[2][1][0] = 2./3.;
    wijk[2][2][0] = 2./3.;
    wijk[2][3][0] = 2./3.;
    wijk[2][4][0] = 2./3.;
    wijk[2][5][0] = 2./3.;

    wijk[0][0][1] = 1./15.;
    wijk[0][1][1] = 1./15.;
    wijk[0][2][1] = 1./15.;
    wijk[0][3][1] = 1./15.;
    wijk[0][4][1] = 1./15.;
    wijk[0][5][1] = 1./15.;
    wijk[1][0][1] = 1./15.;
    wijk[1][1][1] = 1./15.;
    wijk[1][2][1] = 1./15.;
    wijk[1][3][1] = 1./15.;
    wijk[1][4][1] = 1./15.;
    wijk[1][5][1] = 1./15.;
    wijk[0][0][1] = 1./15.;
    wijk[1][0][1] = 1./15.;
    wijk[2][0][1] = 1./15.;
    wijk[3][0][1] = 1./15.;
    wijk[4][0][1] = 1./15.;
    wijk[5][0][1] = 1./15.;
    wijk[0][1][1] = 1./15.;
    wijk[1][1][1] = 1./15.;
    wijk[2][1][1] = 1./15.;
    wijk[3][1][1] = 1./15.;
    wijk[4][1][1] = 1./15.;
    wijk[5][1][1] = 1./15.;*/

    wijk[0][2][0] = 2./3.;
    wijk[0][1][1] = 1./15.;
    wijk[2][2][1] = 4./15.;
    wijk[0][2][1] = -2./15.;
    wijk[1][2][1] = -2./15.;
    wijk[2][0][1] = -2./15.;
    wijk[2][1][1] = -2./15.;
    wijk[4][0][2] = -(1./30.)*Math.sqrt(1.5);
    wijk[4][1][2] = -(1./30.)*Math.sqrt(1.5);
    wijk[4][2][2] = (1./15.)*Math.sqrt(1.5);
    wijk[3][0][3] = -(1./30.)*Math.sqrt(1.5);
    wijk[3][1][3] = -(1./30.)*Math.sqrt(1.5);
    wijk[3][2][3] = (1./15.)*Math.sqrt(1.5);
    wijk[0][0][4] = -(1./30.)*Math.sqrt(1.5);
    wijk[0][1][4] = -(1./30.)*Math.sqrt(1.5);
    wijk[0][2][4] = (1./15.)*Math.sqrt(1.5);
    wijk[1][0][4] = (1./30.)*Math.sqrt(1.5);
    wijk[1][1][4] = (1./30.)*Math.sqrt(1.5);
    wijk[1][2][4] = -(1./15.)*Math.sqrt(1.5);
    wijk[5][0][5] = -(1./30.)*Math.sqrt(1.5);
    wijk[5][1][5] = -(1./30.)*Math.sqrt(1.5);
    wijk[5][2][5] = (1./15.)*Math.sqrt(1.5);
    wijk[5][0][6] = -(1./30.)*Math.sqrt(1.5);
    wijk[5][1][6] = -(1./30.)*Math.sqrt(1.5);
    wijk[5][2][6] = (1./15.)*Math.sqrt(1.5);
    wijk[4][4][7] = 1./60.;
    wijk[3][4][8] = 1./60.;
    wijk[0][4][9] = 1./10.;
    wijk[1][4][9] = -1./10.;
    wijk[5][4][10] = 1./10.;
    wijk[0][3][11] = -(1./15.)*Math.sqrt(1.5);
    wijk[1][3][11] = -(1./15.)*Math.sqrt(1.5);
    wijk[2][3][11] = (2./15.)*Math.sqrt(1.5);
    wijk[4][3][12] = 1./60.;
    wijk[3][3][13] = 1./60.;
    wijk[0][3][14] = 1./10.;
    wijk[1][3][14] = -1./10.;
    wijk[5][3][15] = 1./10.;
    wijk[0][0][16] = -(1./30.)*Math.sqrt(1.5);
    wijk[1][0][16] = -(1./30.)*Math.sqrt(1.5);
    wijk[0][1][16] = (1./30.)*Math.sqrt(1.5);
    wijk[1][1][16] = (1./30.)*Math.sqrt(1.5);
    wijk[2][0][16] = (1./15.)*Math.sqrt(1.5);
    wijk[2][1][16] = -(1./15.)*Math.sqrt(1.5);
    wijk[4][0][17] = 1./20.;
    wijk[4][1][17] = -1./20.;
    wijk[3][0][18] = 1./20.;
    wijk[3][1][18] = -1./20.;
    wijk[0][0][19] = 1./20.;
    wijk[1][1][19] = 1./20.;
    wijk[0][1][19] = -1./20.;
    wijk[1][0][19] = -1./20.;
    wijk[5][0][20] = 1./20.;
    wijk[5][1][20] = -1./20.;
    wijk[0][5][21] = -(1./15.)*Math.sqrt(1.5);
    wijk[1][5][21] = -(1./15.)*Math.sqrt(1.5);
    wijk[2][5][21] = (2./15.)*Math.sqrt(1.5);
    wijk[4][5][22] = 1./10.;
    wijk[3][5][23] = 1./10.;
    wijk[0][5][24] = 1./10.;
    wijk[1][5][24] = -1./10.;
    wijk[5][5][25] = 1./10.;
  }


  public double[][] getTransformationMatrixGforMacrostrain() {
    double[][] gjk = new double[6][26];
    for (int i = 0; i < 6; i++) {
      gjk[i][0] = coefficient[0][i][0][0][0];
      gjk[i][1] = coefficient[0][i][1][0][0];
      gjk[i][2] = coefficient[0][i][1][0][1];
      gjk[i][3] = coefficient[1][i][1][0][1];
      gjk[i][4] = coefficient[0][i][1][0][2];
      gjk[i][5] = coefficient[1][i][1][0][2];
      gjk[i][6] = coefficient[0][i][1][1][0];
      gjk[i][7] = coefficient[0][i][1][1][1];
      gjk[i][8] = coefficient[1][i][1][1][1];
      gjk[i][9] = coefficient[0][i][1][1][2];
      gjk[i][10] = coefficient[1][i][1][1][2];

      gjk[i][11] = coefficient[2][i][1][1][0];
      gjk[i][12] = coefficient[2][i][1][1][1];
      gjk[i][13] = coefficient[3][i][1][1][1];
      gjk[i][14] = coefficient[2][i][1][1][2];
      gjk[i][15] = coefficient[3][i][1][1][2];
      gjk[i][16] = coefficient[0][i][1][2][0];
      gjk[i][17] = coefficient[0][i][1][2][1];
      gjk[i][18] = coefficient[1][i][1][2][1];
      gjk[i][19] = coefficient[0][i][1][2][2];
      gjk[i][20] = coefficient[1][i][1][2][2];

      gjk[i][21] = coefficient[2][i][1][2][0];
      gjk[i][22] = coefficient[2][i][1][2][1];
      gjk[i][23] = coefficient[3][i][1][2][1];
      gjk[i][24] = coefficient[2][i][1][2][2];
      gjk[i][25] = coefficient[3][i][1][2][2];
    }
    return gjk;
  }

  public double[] computeMacroStrain() {

    if (getFilePar().isComputingDerivate())
      return null;

    double[] macrostrain = new double[numberStrainParameters];

    double[][] gjk = getTransformationMatrixGforMacrostrain();

    // ei = sum j=1,6 (sum k=0,25 (wijk gjk))
    for (int i = 0; i < 6; i++) {
      macrostrain[i] = 0.0;
      for (int j = 0; j < 6; j++) {
        for (int k = 0; k < 26; k++) {
          macrostrain[i] += wijk[i][j][k] * gjk[j][k];
        }
      }
      Misc.println("Macrostrain" + strainchoice[i] + ": " + macrostrain[i]);
    }
/*    double va = 0.0, b = 0.0;

    double fnorm = 0.0;
    double resolutionR = Constants.ODFresolution * Constants.DEGTOPI;

    for (int i = 0; i < numberStrainParameters; i++)
      macrostrain[i] = 0.0;

    b = resolutionR / 2.0;

    for (double gamma = b; gamma <= Constants.PI2; gamma += resolutionR) {

      for (double beta = b; beta <= Constants.PI; beta += resolutionR) {

        va = resolutionR * resolutionR * (Math.cos(beta - b) - Math.cos(beta + b));

        for (double alpha = b; alpha <= Constants.PI2; alpha += resolutionR) {
          double[] fn = Angles.trasformInSampleRefFrame(getODF(alpha, beta, gamma),
                  alpha, beta, gamma);
          for (int i = 0; i < numberStrainParameters; i++)
            macrostrain[i] += fn[i] * va;
        }
      }
    }
    Misc.println("Phase: " + getParent().toXRDcatString());
    for (int i = 0; i < numberStrainParameters; i++) {
      macrostrain[i] /= (8.0 * Constants.PI * Constants.PI);

      Misc.println("Macrostrain" + strainchoice[i] + ": " + macrostrain[i]);
    }*/



    return macrostrain;
  }

  public JOptionsDialog getOptionsDialog(Frame parent) {
    JOptionsDialog adialog = new PopaBalzarHarmonicStrainModel.JHStrainOptionsD(parent, this);
    return adialog;
  }

  class JHStrainOptionsD extends JOptionsDialog {

    JComboBox symmetryCB;
    JComboBox strainCB;
    PopaBalzarHarmonicStrainModel.HarmonicPane harmonicCoefficientP;

    public JHStrainOptionsD(Frame parent, XRDcat obj) {

      super(parent, obj);

      principalPanel.setLayout(new BorderLayout(6, 6));
      JPanel jPanel8 = new JPanel();
      jPanel8.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 6));
      principalPanel.add(BorderLayout.NORTH, jPanel8);
      jPanel8.add(new JLabel("Sample symmetry: "));
      symmetryCB = new JComboBox();
      for (int i = 0; i < symmetrychoice.length; i++)
        symmetryCB.addItem(symmetrychoice[i]);
      symmetryCB.setToolTipText("Set up expected sample symmetry");
      jPanel8.add(symmetryCB);

      harmonicCoefficientP = new PopaBalzarHarmonicStrainModel.HarmonicPane(parent, false);
      JPanel jp3 = new JPanel();
      jp3.setLayout(new BorderLayout());
      jp3.setBorder(new TitledBorder(
              new BevelBorder(BevelBorder.LOWERED), "Harmonic coefficients"));
      principalPanel.add(BorderLayout.CENTER, jp3);
      jp3.add("Center", harmonicCoefficientP);

      jPanel8 = new JPanel();
      jPanel8.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 6));
      jp3.add(BorderLayout.NORTH, jPanel8);
      jPanel8.add(new JLabel("Strain parameter: "));
      strainCB = new JComboBox();
      for (int i = 0; i < strainchoice.length; i++)
        strainCB.addItem(strainchoice[i]);
      strainCB.setToolTipText("Choose the strain element");
      jPanel8.add(strainCB);

      JPanel jp1 = new JPanel();
      jp1.setLayout(new BorderLayout());
      jp1.setBorder(new TitledBorder(
              new BevelBorder(BevelBorder.LOWERED), "Options"));
      principalPanel.add(BorderLayout.SOUTH, jp1);

      jp3 = new JPanel();
      jp3.setLayout(new FlowLayout());
      jp1.add(BorderLayout.CENTER, jp3);
      jp3.add(new JLabel("Export PFs for "));
      JButton jb;
      jp3.add(jb = new JButton("Beartex"));
      jb.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          exportPFsinBEARTEXformat();
        }
      });
      jb.setToolTipText("Press this to save the PFs using the Beartex format");
      jp3.add(jb = new JButton("Compute macrostrain"));
      jb.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          computeMacroStrain();
        }
      });
      jb.setToolTipText("Press this to compute the macrostrain, output in the console");

      jp3 = new JPanel();
      jp3.setLayout(new FlowLayout());
      jp1.add(BorderLayout.SOUTH, jp3);
      jp3.add(new JLabel("Export Coeffs for "));
      jp3.add(jb = new JButton("Beartex"));
      jb.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          exportCoeffinBEARTEXformat();
        }
      });
      jb.setToolTipText("Press this to save the coefficients in the Beartex format");

      setTitle("Harmonic strain options panel");
      initParameters();
      pack();

      symmetryCB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          setSampleSymmetry(symmetryCB.getSelectedItem().toString());
          applySymmetryRules();
        }
      });

      strainCB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          changeHarmonicCoeff(strainCB.getSelectedIndex());
        }
      });

      harmonicCoefficientP.initListener();
      harmonicCoefficientP.setSliderValue(expansionDegree);
    }

    public void initParameters() {
      applySymmetryRules();
      symmetryCB.setSelectedItem(getSampleSymmetry());
      strainCB.setSelectedItem(strainchoice[0]);
      harmonicCoefficientP.setExpansionSlider(0, 16);
      harmonicCoefficientP.setList(XRDparent, 0);
    }

    public void retrieveParameters() {
      setSampleSymmetry(symmetryCB.getSelectedItem().toString());
      harmonicCoefficientP.retrieveparlist();
    }

    public void changeHarmonicCoeff(int index) {
//			harmonicCoefficientP.retrieveparlist();
      harmonicCoefficientP.changeList(XRDparent, index);
    }

    public void exportPFsinBEARTEXformat() {
      final String filename = Utility.browseFilenametoSave(this, "choose a file for PFs in BEARTEX format (.xpc)");
      (new Thread() {
        public void run() {
          PoleFigureOutput pfOutput = new PoleFigureOutput(filename, getPhase());
          pfOutput.computeAndWrite();
        }
      }).start();
    }

    public void exportCoeffinBEARTEXformat() {
/*			final String filename = Misc.browseFilenametoSave(this, "choose a file for Coefficients in BEARTEX format (.hha)");

			Phase phase = getPhase();

			refreshCoefficients();

			BufferedWriter PFwriter = Misc.getWriter(filename);

    	String commentLine =	new String("Harmonic coeeficients from RiSTA computing of phase: " + phase.toXRDcatString());

			try {
				PFwriter.write(commentLine);
    		PFwriter.newLine();

				PFwriter.write(filename);
    		PFwriter.newLine();

    		PFwriter.write("    " + Integer.toXRDcatString(SpaceGroups.getLGNumberSiegfriedConv(phase)) + "    " +
    										Integer.toXRDcatString(getSampleSymmetryValue() + 1));
    		PFwriter.newLine();

//			new String("    1.0000    1.0000    1.0000   90.0000   90.0000   90.0000");
				PFwriter.write(Misc.getFirstHHline(phase));
    		PFwriter.newLine();

    		String firstline = new String("    10.000    40.411     1.000   " + Integer.toXRDcatString(expansionDegree));
				PFwriter.write(firstline);
    		PFwriter.newLine();

    		int k = 0;
			  for (int l = 2; l <= expansionDegree; l += 2) {
					int ml2 = StrainSphericalHarmonics.getN(LGIndex, l-1);
					for (int m = 1; m <= ml2; m++) {
				  	int nl2 = StrainSphericalHarmonics.getN(sampleSymmetry, l-1);
				  	for (int n = 1; n <= nl2; n++) {
							PFwriter.write("  " + Integer.toXRDcatString(l-1) + "  ");
							PFwriter.write(Integer.toXRDcatString(m) + "  ");
							PFwriter.write(Integer.toXRDcatString(n) + "  ");
							PFwriter.write("0.0");
    					PFwriter.newLine();
						}
					}
					ml2 = StrainSphericalHarmonics.getN(LGIndex, l);
					for (int m = 1; m <= ml2; m++) {
				  	int nl2 = StrainSphericalHarmonics.getN(sampleSymmetry, l);
				  	for (int n = 1; n <= nl2; n++) {
							PFwriter.write("  " + Integer.toXRDcatString(l) + "  ");
							PFwriter.write(Integer.toXRDcatString(m) + "  ");
							PFwriter.write(Integer.toXRDcatString(n) + "  ");
							double coeff = coefficient[k++]; // * (2 * l + 1) / Math.sqrt(8.0 * Constants.PI);
							PFwriter.write(Fmt.format(coeff));
    					PFwriter.newLine();
						}
					}
				}
    	} catch (IOException io) {}

    	try {
        PFWriter.flush();
    		PFwriter.close();
    	} catch (IOException io) {} */
    }

  }

  public class HarmonicPane extends JPopaSSListPane {
    public HarmonicPane(Frame parent, boolean showTotal) {
      super(parent, showTotal);
    }

    public void expansionHasChanged(int value) {
      if (!MoreMath.odd(value)) {
        retrieveparlist(selected);
        selected = -1;
        setparameterlist(selected);
        setExpansionDegree(value);
      }
    }

    public void setExpansionSlider(int min, int max) {
      expansionJS.setMaximum(max);
      expansionJS.setMinimum(min);
      expansionJS.setPaintTicks(true);
      expansionJS.setMajorTickSpacing(4);
      expansionJS.setMinorTickSpacing(2);

      expansionJS.setPaintLabels(true);
      expansionJS.setSnapToTicks(true);

      expansionJS.setValue(max);

      expansionJS.setLabelTable(expansionJS.createStandardLabels(4));
    }

  }

}
