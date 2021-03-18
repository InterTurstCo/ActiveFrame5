package net.sf.jasperreports.engine.export.ooxml;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.*;

public class SochiJRDocxExporter extends JRDocxExporter {


	@Override
	protected void exportGrid(JRGridLayout gridLayout, JRPrintElementIndex frameIndex) throws JRException
	{

		CutsInfo xCuts = gridLayout.getXCuts();
		Grid grid = gridLayout.getGrid();
		DocxTableHelper tableHelper = null;

		int rowCount = grid.getRowCount();
		if (rowCount > 0 && grid.getColumnCount() > 63)
		{
			throw new JRException("The DOCX format does not support more than 63 columns in a table.");
		}

		// an empty page is encountered;
		// if it's the first one in a series of consecutive empty pages, emptyPageState == false, otherwise emptyPageState == true
		if(rowCount == 0 && (pageIndex < endPageIndex || !emptyPageState))
		{
			tableHelper =
					new SochiDocxTableHelper(
							jasperReportsContext,
							docWriter,
							xCuts,
							false
					);
			int maxReportIndex = jasperPrintList.size() - 1;

			// while the first and last page in the JasperPrint list need single breaks, all the others require double-breaking
			boolean twice =
					(pageIndex > startPageIndex && pageIndex < endPageIndex && !emptyPageState)
							||(reportIndex < maxReportIndex && pageIndex == endPageIndex);
			tableHelper.getParagraphHelper().exportEmptyPage(pageAnchor, bookmarkIndex, twice);
			tableHelper.exportSection(
					reportIndex < maxReportIndex && pageIndex == endPageIndex,
					jasperPrint.getPageWidth(),
					jasperPrint.getPageHeight()
			);
			bookmarkIndex++;
			emptyPageState = true;
			return;
		}

		tableHelper =
				new SochiDocxTableHelper(
						jasperReportsContext,
						docWriter,
						xCuts,
						frameIndex == null && (reportIndex != 0 || pageIndex != startPageIndex)
				);

		tableHelper.exportHeader();

		for(int row = 0; row < rowCount; row++)
		{
			int emptyCellColSpan = 0;
			//int emptyCellWidth = 0;

			boolean allowRowResize = false;
			int maxBottomPadding = 0; //for some strange reason, the bottom margin affects the row height; subtracting it here
			GridRow gridRow = grid.getRow(row);
			int rowSize = gridRow.size();
			for(int col = 0; col < rowSize; col++)
			{
				JRExporterGridCell gridCell = gridRow.get(col);
				JRLineBox box = gridCell.getBox();
				if (
						box != null
								&& box.getBottomPadding() != null
								&& maxBottomPadding < box.getBottomPadding().intValue()
				)
				{
					maxBottomPadding = box.getBottomPadding().intValue();
				}

				allowRowResize =
						flexibleRowHeight
								&& (allowRowResize
								|| (gridCell.getElement() instanceof JRPrintText
								|| (gridCell.getType() == JRExporterGridCell.TYPE_OCCUPIED_CELL
								&& ((OccupiedGridCell)gridCell).getOccupier().getElement() instanceof JRPrintText)
						)
						);
			}
			int rowHeight = gridLayout.getRowHeight(row) - maxBottomPadding;

			tableHelper.exportRowHeader(
					rowHeight,
					allowRowResize
			);

			for(int col = 0; col < rowSize; col++)
			{
				JRExporterGridCell gridCell = gridRow.get(col);
				if (gridCell.getType() == JRExporterGridCell.TYPE_OCCUPIED_CELL)
				{
					if (emptyCellColSpan > 0)
					{
						//tableHelper.exportEmptyCell(gridCell, emptyCellColSpan);
						emptyCellColSpan = 0;
						//emptyCellWidth = 0;
					}

					OccupiedGridCell occupiedGridCell = (OccupiedGridCell)gridCell;
					ElementGridCell elementGridCell = (ElementGridCell)occupiedGridCell.getOccupier();
					tableHelper.exportOccupiedCells(elementGridCell, startPage, bookmarkIndex, pageAnchor);
					if(startPage)
					{
						// increment the bookmarkIndex for the first cell in the sheet, due to page anchor creation
						bookmarkIndex++;
					}
					col += elementGridCell.getColSpan() - 1;
				}
				else if(gridCell.getType() == JRExporterGridCell.TYPE_ELEMENT_CELL)
				{
					if (emptyCellColSpan > 0)
					{
						//writeEmptyCell(tableHelper, gridCell, emptyCellColSpan, emptyCellWidth, rowHeight);
						emptyCellColSpan = 0;
						//emptyCellWidth = 0;
					}

					JRPrintElement element = gridCell.getElement();

					if (element instanceof JRPrintLine)
					{
						exportLine(tableHelper, (JRPrintLine)element, gridCell);
					}
					else if (element instanceof JRPrintRectangle)
					{
						exportRectangle(tableHelper, (JRPrintRectangle)element, gridCell);
					}
					else if (element instanceof JRPrintEllipse)
					{
						exportEllipse(tableHelper, (JRPrintEllipse)element, gridCell);
					}
					else if (element instanceof JRPrintImage)
					{
						exportImage(tableHelper, (JRPrintImage)element, gridCell);
					}
					else if (element instanceof JRPrintText)
					{
						exportText(tableHelper, (JRPrintText)element, gridCell);
					}
					else if (element instanceof JRPrintFrame)
					{
						exportFrame(tableHelper, (JRPrintFrame)element, gridCell);
					}
					else if (element instanceof JRGenericPrintElement)
					{
						exportGenericElement(tableHelper, (JRGenericPrintElement)element, gridCell);
					}

					col += gridCell.getColSpan() - 1;
				}
				else
				{
					emptyCellColSpan++;
					//emptyCellWidth += gridCell.getWidth();
					tableHelper.exportEmptyCell(gridCell, 1, startPage, bookmarkIndex, pageAnchor);
					if(startPage)
					{
						// increment the bookmarkIndex for the first cell in the sheet, due to page anchor creation
						bookmarkIndex++;
					}
				}
				startPage = false;
			}

//			if (emptyCellColSpan > 0)
//			{
//				//writeEmptyCell(tableHelper, null, emptyCellColSpan, emptyCellWidth, rowHeight);
//			}

			tableHelper.exportRowFooter();
		}

		tableHelper.exportFooter(
				frameIndex == null && reportIndex != jasperPrintList.size() - 1 && pageIndex == endPageIndex ,
				jasperPrint.getPageWidth(),
				jasperPrint.getPageHeight()
		);
		// if a non-empty page was exported, the series of previous empty pages is ended
		emptyPageState = false;
	}

}
