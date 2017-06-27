package ru.intertrust.cm.core.business.impl.reportpostprocessors;

import java.io.File;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTRel;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.FooterReference;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Text;

import ru.intertrust.cm.core.business.api.ReportPostProcessor;
import ru.intertrust.cm.core.model.ReportServiceException;

public class FooterPageNumberingReportPostProcessor implements
		ReportPostProcessor {

	@Override
	public void format(File reportFile) {

		try {
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
					.load(reportFile);

			addFooterToDocument(wordMLPackage);

			wordMLPackage.save(reportFile);
		} catch (Exception ex) {
			throw new ReportServiceException(
					"Error post edit jasper report file", ex);
		}
	}

	/**
	 * Добавляем футер в документ
	 * 
	 * @param wordMLPackage
	 * @param docVersionNumber
	 * @throws InvalidFormatException
	 */
	private static void addFooterToDocument(
			WordprocessingMLPackage wordMLPackage)
			throws InvalidFormatException {
		ObjectFactory factory = Context.getWmlObjectFactory();
		Relationship relationship = createFooterPart(wordMLPackage, factory);
		createFooterReference(relationship, wordMLPackage, factory);
	}

	/**
	 * Метод создаёт FooterPart и устанавливает связь с документом Дальше
	 * добавляем логику в FooterPart
	 * 
	 * @param wordMLPackage
	 *            the word ml package
	 * @param versionNumber
	 * @param documentNumber
	 * @return the relationship
	 * @throws InvalidFormatException
	 *             the invalid format exception
	 */
	private static Relationship createFooterPart(
			WordprocessingMLPackage wordMLPackage, ObjectFactory factory)
			throws InvalidFormatException {
		FooterPart footerPart = new FooterPart();
		footerPart.setPackage(wordMLPackage);
		footerPart.setJaxbElement(createFooter(factory));
		return wordMLPackage.getMainDocumentPart().addTargetPart(footerPart);
	}

	/**
	 * @param content
	 * @return
	 */
	private static Ftr createFooter(ObjectFactory factory) {
		Ftr footer = factory.createFtr();
		P paragraph = factory.createP();
		R run = factory.createR();
		/*
		 * Change the font size to 8 points(the font size is defined to be in
		 * half-point size so set the value as 16).
		 */
		RPr rpr = new RPr();
		HpsMeasure size = new HpsMeasure();
		size.setVal(BigInteger.valueOf(16));
		rpr.setSz(size);
		run.setRPr(rpr);
		paragraph.getContent().add(run);
		footer.getContent().add(paragraph);
		P pageNumParagraph = factory.createP();
		addFieldBegin(factory, pageNumParagraph);
		addPageNumberField(factory, pageNumParagraph);
		addFieldEnd(factory, pageNumParagraph);
		footer.getContent().add(pageNumParagraph);
		return footer;
	}

	/**
	 * Создаём поле с отображением страницы
	 * 
	 * @param paragraph
	 */
	private static void addPageNumberField(ObjectFactory factory, P paragraph) {
		R run = factory.createR();
		PPr ppr = new PPr();
		Jc jc = new Jc();
		jc.setVal(JcEnumeration.CENTER);
		ppr.setJc(jc);
		paragraph.setPPr(ppr);
		Text txt = new Text();
		txt.setSpace("preserve");
		txt.setValue(" PAGE   \\* MERGEFORMAT ");
		run.getContent().add(factory.createRInstrText(txt));
		paragraph.getContent().add(run);
	}

	/**
	 * Добавдяем "Начало" к полю
	 * 
	 * @param paragraph
	 */
	private static void addFieldBegin(ObjectFactory factory, P paragraph) {
		R run = factory.createR();
		FldChar fldchar = factory.createFldChar();
		fldchar.setFldCharType(STFldCharType.BEGIN);
		run.getContent().add(fldchar);
		paragraph.getContent().add(run);
	}

	/**
	 * Добавдяем "Конец" к полю
	 * 
	 * @param paragraph
	 */
	private static void addFieldEnd(ObjectFactory factory, P paragraph) {
		FldChar fldcharend = factory.createFldChar();
		fldcharend.setFldCharType(STFldCharType.END);
		R run3 = factory.createR();
		run3.getContent().add(fldcharend);
		paragraph.getContent().add(run3);
	}

	/**
	 * 
	 * @param relationship
	 * @param wordMLPackage
	 * @param factory
	 */
	private static void createFooterReference(Relationship relationship,
			WordprocessingMLPackage wordMLPackage, ObjectFactory factory) {
		List<SectionWrapper> sections = wordMLPackage.getDocumentModel()
				.getSections();
		SectPr sectionProperties = sections.get(sections.size() - 1)
				.getSectPr();
		// There is always a section wrapper, but it might not contain a sectPr
		if (sectionProperties == null) {
			sectionProperties = factory.createSectPr();
			wordMLPackage.getMainDocumentPart().addObject(sectionProperties);
			sections.get(sections.size() - 1).setSectPr(sectionProperties);
		}

		/*
		 * Remove footer if it already exists.
		 */
		List<CTRel> relations = sectionProperties.getEGHdrFtrReferences();
		Iterator<CTRel> relationsItr = relations.iterator();
		while (relationsItr.hasNext()) {
			CTRel relation = relationsItr.next();
			if (relation instanceof FooterReference) {
				relationsItr.remove();
			}
		}

		FooterReference footerReference = factory.createFooterReference();
		footerReference.setId(relationship.getId());
		footerReference.setType(HdrFtrRef.DEFAULT);
		sectionProperties.getEGHdrFtrReferences().add(footerReference);
		FooterReference firstPagefooterRef = factory.createFooterReference();
		firstPagefooterRef.setId(relationship.getId());
		firstPagefooterRef.setType(HdrFtrRef.FIRST);
		sectionProperties.getEGHdrFtrReferences().add(firstPagefooterRef);
	}

}
