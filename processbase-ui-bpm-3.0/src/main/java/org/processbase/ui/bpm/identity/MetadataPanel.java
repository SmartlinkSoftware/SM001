/**
 * Copyright (C) 2010 PROCESSBASE Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.processbase.ui.bpm.identity;

import java.util.List;

import org.ow2.bonita.facade.identity.ProfileMetadata;
import org.processbase.ui.core.Constants;
import org.processbase.ui.core.ProcessbaseApplication;
import org.processbase.ui.core.template.ConfirmDialog;
import org.processbase.ui.core.template.PagedTablePanel;
import org.processbase.ui.core.template.TableLinkButton;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;

/**
 * 
 * @author marat gubaidullin
 */
public class MetadataPanel extends PagedTablePanel implements
		Button.ClickListener {

	public MetadataPanel() {
		super();
	}

	@Override
	public void initUI() {
		super.initUI();
		table.addContainerProperty("name", TableLinkButton.class, null,
				getText("tableCaptionName"), null, null);
		table.addContainerProperty("label", String.class, null,
				getText("tableCaptionLabel"), null, null);
		table.setColumnExpandRatio("label", 1);
		table.addContainerProperty("actions", TableLinkButton.class, null,
				getText("tableCaptionActions"), null, null);
		table.setImmediate(true);

		setInitialized(true);
	}

	@Override
	public int load(int startPosition, int maxResults) {
		int results = 0;

		try {
			table.removeAllItems();
			List<ProfileMetadata> metadatas = ProcessbaseApplication
					.getCurrent().getBpmModule()
					.getProfileMetadata(startPosition, maxResults);
			results = metadatas.size();

			for (ProfileMetadata metadata : metadatas) {
				Item woItem = table.addItem(metadata);
				TableLinkButton teb = new TableLinkButton(metadata.getName(),
						"", null, metadata, this, Constants.ACTION_OPEN);
				woItem.getItemProperty("name").setValue(teb);
				woItem.getItemProperty("label").setValue(metadata.getLabel());
				TableLinkButton tlb = new TableLinkButton(
						ProcessbaseApplication.getCurrent().getPbMessages()
								.getString("btnDelete"), "icons/cancel.png",
						metadata, this, Constants.ACTION_DELETE);
				woItem.getItemProperty("actions").setValue(tlb);
			}
			table.setSortContainerPropertyId("username");
			table.setSortAscending(false);
			table.sort();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return results;
	}

	public void buttonClick(ClickEvent event) {
		if (event.getButton() instanceof TableLinkButton) {
			TableLinkButton execBtn = (TableLinkButton) event.getButton();
			ProfileMetadata metadata = (ProfileMetadata) execBtn
					.getTableValue();
			if (execBtn.getAction().equals(Constants.ACTION_DELETE)) {
				try {
					removeMetadata(metadata);
				} catch (Exception ex) {
					ex.printStackTrace();
					showError(ex.getMessage());
					throw new RuntimeException(ex);
				}
			} else if (execBtn.getAction().equals(Constants.ACTION_OPEN)) {
				MetadataWindow nmw = new MetadataWindow(metadata);
				nmw.addListener((Window.CloseListener) this);
				getWindow().addWindow(nmw);
				nmw.initUI();
			}

		}
	}

	private void removeMetadata(final ProfileMetadata metadata) {
		ConfirmDialog.show(getApplication().getMainWindow(),
				getText("windowCaptionConfirm"), getText("removeMetadata")
						+ "?", getText("btnYes"), getText("btnNo"),
				new ConfirmDialog.Listener() {

					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							try {
								ProcessbaseApplication
										.getCurrent()
										.getBpmModule()
										.removeProfileMetadataByUUID(
												metadata.getUUID());
								table.removeItem(metadata);
							} catch (Exception ex) {
								ex.printStackTrace();
								showError(ex.getMessage());
								throw new RuntimeException(ex);
							}
						}
					}
				});
	}
}
