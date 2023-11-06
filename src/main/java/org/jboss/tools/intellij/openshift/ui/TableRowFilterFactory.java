/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.ui;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class TableRowFilterFactory {

  private TableRowFilterFactory() {}

  public static DocumentAdapterFactory createOn(JTable table) {
    TableRowSorter<? extends TableModel> sorter = createRowSorter(table);
    return new DocumentAdapterFactory(sorter);
  }

  private static TableRowSorter<? extends TableModel> createRowSorter(JTable table) {
    RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
    if (rowSorter == null) {
      table.setAutoCreateRowSorter(true);
      rowSorter = table.getRowSorter();
    }
    return (TableRowSorter<? extends TableModel>) rowSorter;
  }

  public static class DocumentAdapterFactory {

    private final TableRowSorter<? extends TableModel> rowSorter;

    public DocumentAdapterFactory(TableRowSorter<? extends TableModel> rowSorter) {
      this.rowSorter = rowSorter;
    }

    public void usingInput(Document document) {
      DocumentListener documentListener = new DocumentAdapter();
      document.addDocumentListener(documentListener);
    }

    private static String getFilterText(Document document) {
      try {
        return document.getText(0, document.getLength());
      } catch (BadLocationException ex) {
        return null;
      }
    }

    private class DocumentAdapter implements DocumentListener {
      @Override
      public void insertUpdate(DocumentEvent e) {
        update(e);
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        update(e);
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        update(e);
      }

      private void update(DocumentEvent e) {
        javax.swing.text.Document document = e.getDocument();
        String filterText = getFilterText(document);
        if (filterText == null
          || filterText.trim().isEmpty()) {
          rowSorter.setRowFilter(null);
        } else {
          rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText));
        }
      }
    }

  }
}
