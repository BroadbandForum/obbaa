/*
 * Copyright 2020 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.device.adapter;

import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.NOT_APPLICABLE;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.PERCENTAGE;

import java.util.ArrayList;
import java.util.List;

public final class FactoryGarmentTag {

    private final int m_totalNumberOfStandardModules;
    private final int m_numberOfReusedStandardModules;
    private int m_adherencePercentage;
    private List<String> m_deviatedStdModules = new ArrayList();
    private List<String> m_augmentedStdModules = new ArrayList();
    private int m_percentageDeviatedStdModules;
    private int m_numberOfDeviatedStdModules;
    private int m_numberOfAugmentedStdModules;
    private int m_numberOfModulesOfVendorAdapter;
    private int m_percentageAugmentedStdModules;

    public FactoryGarmentTag(int totalNumberOfStandardModules, int numberOfModulesOfVendorAdapter, int numberOfReusedStandardModules,
                             List<String> deviatedStandardModules, List<String> augmentedStdModules) {
        this.m_totalNumberOfStandardModules = totalNumberOfStandardModules;
        this.m_numberOfReusedStandardModules = numberOfReusedStandardModules;
        this.m_deviatedStdModules = deviatedStandardModules;
        this.m_numberOfModulesOfVendorAdapter = numberOfModulesOfVendorAdapter;
        this.m_augmentedStdModules = augmentedStdModules;

    }


    public int getTotalNumberOfStandardModules() {
        return m_totalNumberOfStandardModules;
    }


    public int getAdherencePercentage() {
        this.m_adherencePercentage = ((this.m_numberOfReusedStandardModules * 100) / this.m_totalNumberOfStandardModules);
        return m_adherencePercentage;
    }


    public List<String> getDeviatedStdModules() {
        return m_deviatedStdModules;
    }


    public List<String> getAugmentedStdModules() {
        return m_augmentedStdModules;
    }


    public String getPercentageDeviatedStdModules() {
        this.m_numberOfDeviatedStdModules = m_deviatedStdModules.size();
        if (this.m_numberOfReusedStandardModules == 0) {
            return NOT_APPLICABLE;
        } else {
            this.m_percentageDeviatedStdModules = ((this.m_numberOfDeviatedStdModules * 100) / this.m_numberOfReusedStandardModules);
            return String.valueOf(m_percentageDeviatedStdModules).concat(PERCENTAGE);
        }
    }

    public String getPercentageAugmentedStdModules() {
        this.m_numberOfAugmentedStdModules = m_augmentedStdModules.size();
        if (this.m_numberOfReusedStandardModules == 0) {
            return NOT_APPLICABLE;
        } else {
            m_percentageAugmentedStdModules = ((this.m_numberOfAugmentedStdModules * 100) / this.m_numberOfReusedStandardModules);
            return String.valueOf(m_percentageAugmentedStdModules).concat(PERCENTAGE);
        }
    }


    public int getNumberOfModulesOfVendorAdapter() {
        return m_numberOfModulesOfVendorAdapter;
    }


    @Override
    public String toString() {
        return "FactoryGarmentTag{"
                + "m_totalNumberOfStandardModules='" + m_totalNumberOfStandardModules
                + "m_numberOfModulesOfVendorAdapter='" + m_numberOfModulesOfVendorAdapter
                + ", m_adherencePercentage='" + m_adherencePercentage
                + ", m_numberOfReusedStandardModules='" + m_numberOfReusedStandardModules
                + ", m_percentageDeviatedStdModules='" + m_percentageDeviatedStdModules
                + ", m_deviatedStdModules='" + m_deviatedStdModules
                + ", m_augmentedStdModules='" + m_augmentedStdModules
                + ", m_percentageAugmentedStdModules='" + m_percentageAugmentedStdModules
                + '}';
    }

}
