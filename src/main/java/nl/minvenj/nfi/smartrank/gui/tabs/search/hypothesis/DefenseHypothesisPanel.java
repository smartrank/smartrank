/*
 * Copyright (C) 2015 Netherlands Forensic Institute
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package nl.minvenj.nfi.smartrank.gui.tabs.search.hypothesis;

import nl.minvenj.nfi.smartrank.domain.Contributor;
import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.messages.data.DefenseHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.UpdateDefenseContributorMessage;
import nl.minvenj.nfi.smartrank.messages.data.UpdateDefenseNoncontributorMessage;
import nl.minvenj.nfi.smartrank.messages.data.UpdateDefenseUnknownsMessage;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public class DefenseHypothesisPanel extends HypothesisPanel {

    public DefenseHypothesisPanel() {
        super("defenseContributorTable");
    }

    @RavenMessageHandler(DefenseHypothesisMessage.class)
    public void onNewDefenseHypothesis(final DefenseHypothesis hypothesis) {
        setHypothesis(hypothesis);
    }

    @Override
    public void onUpdateContributor(final boolean isContributor, final Sample sample, final double dropout) {
        if (isContributor) {
            MessageBus.getInstance().send(this, new UpdateDefenseContributorMessage(new Contributor(sample, dropout)));
        } else {
            MessageBus.getInstance().send(this, new UpdateDefenseNoncontributorMessage(new Contributor(sample, dropout)));
        }
    }

    @Override
    public void onUpdateUnknowns(final int count, final double dropout) {
        MessageBus.getInstance().send(this, new UpdateDefenseUnknownsMessage(count, dropout));
    }
}
