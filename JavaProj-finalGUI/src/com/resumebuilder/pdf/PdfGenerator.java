package com.resumebuilder.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.resumebuilder.backend.customsection.AbstractCustomSection;
import com.resumebuilder.backend.model.*;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public class PdfGenerator {


    
    public static void generate(Resume resume, java.io.OutputStream out) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 50);
        PdfWriter writer = PdfWriter.getInstance(document, out);
        writer.setPageEvent(new PageNumbersInfo());
        document.open();

        Color themeColor = getThemeColor(resume.getColorTheme());
        Color lightTheme = getLightThemeColor(themeColor);
        
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, Font.NORMAL, themeColor);
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.ITALIC, Color.DARK_GRAY);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.NORMAL, themeColor);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Font.NORMAL, Color.BLACK);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
        Font italicFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Font.ITALIC, Color.GRAY);

        String template = resume.getTemplateName();
        if ("Modern".equalsIgnoreCase(template)) {
            generateModernTemplate(document, resume, themeColor, lightTheme, titleFont, subTitleFont, sectionFont, normalFont, boldFont, smallFont, italicFont);
        } else if ("Creative".equalsIgnoreCase(template)) {
            generateCreativeTemplate(document, resume, themeColor, lightTheme, titleFont, subTitleFont, sectionFont, normalFont, boldFont, smallFont, italicFont);
        } else if ("Minimal".equalsIgnoreCase(template)) {
            generateMinimalTemplate(document, resume, themeColor, lightTheme, titleFont, subTitleFont, sectionFont, normalFont, boldFont, smallFont, italicFont);
        } else {
            generateClassicTemplate(document, resume, themeColor, lightTheme, titleFont, subTitleFont, sectionFont, normalFont, boldFont, smallFont, italicFont);
        }

        document.close();
    }

    private static Color getThemeColor(String themeName) {
        if ("Green".equalsIgnoreCase(themeName)) return new Color(34, 139, 34);
        if ("Grey".equalsIgnoreCase(themeName)) return new Color(60, 60, 60);
        if ("Red".equalsIgnoreCase(themeName)) return new Color(153, 27, 27);
        if ("Purple".equalsIgnoreCase(themeName)) return new Color(102, 51, 153);
        if ("Teal".equalsIgnoreCase(themeName)) return new Color(0, 128, 128);
        if ("Orange".equalsIgnoreCase(themeName)) return new Color(204, 85, 0);
        return new Color(0, 102, 204); // Default Blue
    }
    
    private static Color getLightThemeColor(Color themeColor) {
        int r = Math.min(255, themeColor.getRed() + 200);
        int g = Math.min(255, themeColor.getGreen() + 200);
        int b = Math.min(255, themeColor.getBlue() + 200);
        return new Color(r, g, b);
    }
    

    private static void generateClassicTemplate(Document doc, Resume r, Color theme, Color lightTheme, 
            Font titleFont, Font subTitleFont, Font sectionFont, Font normalFont, Font boldFont, Font smallFont, Font italicFont) 
            throws DocumentException, IOException {
        
        // Header with Photo and Info (Horizontal Layout)
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{25, 75}); // Photo 25%, Info 75%
        headerTable.setSpacingAfter(15);
        
        // Left Cell - Photo
        PdfPCell photoCell = new PdfPCell();
        photoCell.setBorder(Rectangle.NO_BORDER);
        photoCell.setPadding(10);
        photoCell.setVerticalAlignment(Element.ALIGN_TOP);
        photoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        if (r.getPhotoPath() != null && !r.getPhotoPath().isEmpty()) {
            try {
                Image img = Image.getInstance(r.getPhotoPath());
                img.scaleToFit(100, 100);
                img.setAlignment(Element.ALIGN_CENTER);
                photoCell.addElement(img);
            } catch (Exception e) { 
                // No photo available
            }
        }
        
        // Right Cell - Name, Title, Contact
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPadding(10);
        infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        
        Paragraph name = new Paragraph(r.getFullName(), titleFont);
        name.setSpacingAfter(5);
        infoCell.addElement(name);
        
        if (r.getTitle() != null && !r.getTitle().isEmpty()) {
            Paragraph title = new Paragraph(r.getTitle(), subTitleFont);
            title.setSpacingAfter(10);
            infoCell.addElement(title);
        }
        
        Font contactFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
        Paragraph contact = new Paragraph();
        contact.add(new Chunk("📧 " + r.getEmail() + "  •  ", contactFont));
        contact.add(new Chunk("📞 " + r.getPhone() + "  •  ", contactFont));
        contact.add(new Chunk("📍 " + r.getAddress(), contactFont));
        infoCell.addElement(contact);
        
        headerTable.addCell(photoCell);
        headerTable.addCell(infoCell);
        doc.add(headerTable);
        
        doc.add(new LineSeparator(2f, 100f, theme, Element.ALIGN_CENTER, -2));
        doc.add(Chunk.NEWLINE);
        
        // Summary
        if (r.getSummary() != null && !r.getSummary().isEmpty()) {
            addEnhancedSection(doc, "PROFESSIONAL SUMMARY", sectionFont, theme);
            Paragraph summary = new Paragraph(r.getSummary(), normalFont);
            summary.setAlignment(Element.ALIGN_JUSTIFIED);
            summary.setSpacingAfter(10);
            doc.add(summary);
        }

          // Skills
        if (!r.getSkillList().isEmpty()) {
            addEnhancedSection(doc, "SKILLS", sectionFont, theme);
            PdfPTable skillsTable = new PdfPTable(3);
            skillsTable.setWidthPercentage(100);
            skillsTable.setSpacingAfter(10);
            
            for (Skill s : r.getSkillList()) {
                PdfPCell cell = new PdfPCell(new Phrase("• " + s.getSkillName(), normalFont));
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPadding(4);
                skillsTable.addCell(cell);
            }
            doc.add(skillsTable);
        }

             // Education
        if (!r.getEducationList().isEmpty()) {
            addEnhancedSection(doc, "EDUCATION", sectionFont, theme);
            for (Education e : r.getEducationList()) {
                Paragraph degree = new Paragraph(e.getDegree(), boldFont);
                degree.setSpacingAfter(2);
                doc.add(degree);
                
                Paragraph uni = new Paragraph();
                uni.add(new Chunk(e.getUniversity(), normalFont));
                uni.add(new Chunk(" | " + e.getYear() + " | CGPA: " + e.getCgpa(), italicFont));
                uni.setSpacingAfter(12);
                doc.add(uni);
            }
        }
        
        
        // Experience
        if (!r.getExperienceList().isEmpty()) {
            addEnhancedSection(doc, "EXPERIENCE", sectionFont, theme);
            for (Experience e : r.getExperienceList()) {
                Paragraph role = new Paragraph(e.getRole(), boldFont);
                role.setSpacingAfter(2);
                doc.add(role);
                
                Paragraph company = new Paragraph();
                company.add(new Chunk(e.getCompany(), normalFont));
                company.add(new Chunk(" | " + e.getDuration(), italicFont));
                company.setSpacingAfter(5);
                doc.add(company);
                
                Paragraph desc = new Paragraph(e.getDescription(), normalFont);
                desc.setAlignment(Element.ALIGN_JUSTIFIED);
                desc.setSpacingAfter(12);
                doc.add(desc);
            }
        }
        
   
        // Projects
        if (!r.getProjectList().isEmpty()) {
            addEnhancedSection(doc, "PROJECTS", sectionFont, theme);
            for (Project p : r.getProjectList()) {
                Paragraph projectTitle = new Paragraph(p.getTitle(), boldFont);
                projectTitle.setSpacingAfter(5);
                doc.add(projectTitle);
                
                Paragraph projectDesc = new Paragraph(p.getDescription(), normalFont);
                projectDesc.setAlignment(Element.ALIGN_JUSTIFIED);
                projectDesc.setSpacingAfter(12);
                doc.add(projectDesc);
            }
        }

        addClassicCustomSections(doc, r, sectionFont, normalFont, theme);
    }


    private static void generateModernTemplate(Document doc, Resume r, Color theme, Color lightTheme,
            Font titleFont, Font subTitleFont, Font sectionFont, Font normalFont, Font boldFont, Font smallFont, Font italicFont)
            throws DocumentException, IOException {
        
        // Two Column Layout
        PdfPTable table = new PdfPTable(new float[]{35, 65});
        table.setWidthPercentage(100);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        
        // LEFT SIDEBAR
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setBackgroundColor(theme);
        leftCell.setPadding(20);
        leftCell.setPaddingTop(30);
        
        // Photo
        if (r.getPhotoPath() != null && !r.getPhotoPath().isEmpty()) {
            try {
                Image img = Image.getInstance(r.getPhotoPath());
                img.scaleToFit(120, 120);
                img.setAlignment(Element.ALIGN_CENTER);
                leftCell.addElement(img);
                leftCell.addElement(Chunk.NEWLINE);
            } catch (Exception e) { }
        }
        
        // Contact Section
        Font sidebarHeading = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.NORMAL, Color.WHITE);
        Font sidebarText = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.WHITE);
        
        Paragraph contactHead = new Paragraph("CONTACT", sidebarHeading);
        contactHead.setSpacingAfter(8);
        leftCell.addElement(contactHead);
        
        leftCell.addElement(new Paragraph("• " + r.getEmail(), sidebarText));
        leftCell.addElement(new Paragraph("• " + r.getPhone(), sidebarText));
        leftCell.addElement(new Paragraph("• " + r.getAddress(), sidebarText));
        leftCell.addElement(Chunk.NEWLINE);
        leftCell.addElement(Chunk.NEWLINE);
        
        // Skills Section
        if (!r.getSkillList().isEmpty()) {
            Paragraph skillsHead = new Paragraph("SKILLS", sidebarHeading);
            skillsHead.setSpacingAfter(8);
            leftCell.addElement(skillsHead);
            
            for (Skill s : r.getSkillList()) {
                Paragraph skill = new Paragraph("• " + s.getSkillName(), sidebarText);
                skill.setSpacingAfter(4);
                leftCell.addElement(skill);
            }
        }
        
        // RIGHT CONTENT
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPadding(20);
        rightCell.setPaddingTop(30);
        
        // Name and Title
        Paragraph name = new Paragraph(r.getFullName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, Font.NORMAL, theme));
        name.setSpacingAfter(5);
        rightCell.addElement(name);
        
        if (r.getTitle() != null && !r.getTitle().isEmpty()) {
            Paragraph title = new Paragraph(r.getTitle(), subTitleFont);
            title.setSpacingAfter(15);
            rightCell.addElement(title);
        }
        
        // Summary
        if (r.getSummary() != null && !r.getSummary().isEmpty()) {
            addModernSection(rightCell, "SUMMARY", sectionFont, theme);
            Paragraph summary = new Paragraph(r.getSummary(), normalFont);
            summary.setAlignment(Element.ALIGN_JUSTIFIED);
            summary.setSpacingAfter(15);
            rightCell.addElement(summary);
        }
        
        // Experience
        if (!r.getExperienceList().isEmpty()) {
            addModernSection(rightCell, "EXPERIENCE", sectionFont, theme);
            for (Experience e : r.getExperienceList()) {
                Paragraph role = new Paragraph(e.getRole(), boldFont);
                role.setSpacingAfter(2);
                rightCell.addElement(role);
                
                Paragraph companyDuration = new Paragraph();
                companyDuration.add(new Chunk(e.getCompany() + " | ", normalFont));
                companyDuration.add(new Chunk(e.getDuration(), italicFont));
                companyDuration.setSpacingAfter(5);
                rightCell.addElement(companyDuration);
                
                Paragraph desc = new Paragraph(e.getDescription(), normalFont);
                desc.setAlignment(Element.ALIGN_JUSTIFIED);
                desc.setSpacingAfter(12);
                rightCell.addElement(desc);
            }
        }
        
        // Education
        if (!r.getEducationList().isEmpty()) {
            addModernSection(rightCell, "EDUCATION", sectionFont, theme);
            for (Education e : r.getEducationList()) {
                Paragraph degree = new Paragraph(e.getDegree(), boldFont);
                degree.setSpacingAfter(2);
                rightCell.addElement(degree);
                
                Paragraph uniInfo = new Paragraph();
                uniInfo.add(new Chunk(e.getUniversity() + " | ", normalFont));
                uniInfo.add(new Chunk(e.getYear() + " | CGPA: " + e.getCgpa(), italicFont));
                uniInfo.setSpacingAfter(12);
                rightCell.addElement(uniInfo);
            }
        }
        
        // Projects
        if (!r.getProjectList().isEmpty()) {
            addModernSection(rightCell, "PROJECTS", sectionFont, theme);
            for (Project p : r.getProjectList()) {
                Paragraph projectTitle = new Paragraph(p.getTitle(), boldFont);
                projectTitle.setSpacingAfter(5);
                rightCell.addElement(projectTitle);
                
                Paragraph projectDesc = new Paragraph(p.getDescription(), normalFont);
                projectDesc.setAlignment(Element.ALIGN_JUSTIFIED);
                projectDesc.setSpacingAfter(12);
                rightCell.addElement(projectDesc);
            }
        }

        addModernCustomSections(rightCell, r, sectionFont, normalFont, theme);
        
        table.addCell(leftCell);
        table.addCell(rightCell);
        doc.add(table);
    }

    private static void generateCreativeTemplate(Document doc, Resume r, Color theme, Color lightTheme,
            Font titleFont, Font subTitleFont, Font sectionFont, Font normalFont, Font boldFont, Font smallFont, Font italicFont)
            throws DocumentException, IOException {
        
        // Creative Header Banner (Horizontal Layout)
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{30, 70}); // Photo 30%, Text 70%
        
        Font headerNameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 32, Font.NORMAL, Color.WHITE);
        Font headerTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.NORMAL, Color.WHITE);
        Font headerContactFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.WHITE);
        
        // Left Cell - Photo
        PdfPCell photoCell = new PdfPCell();
        photoCell.setBorder(Rectangle.NO_BORDER);
        photoCell.setBackgroundColor(theme);
        photoCell.setPadding(25);
        photoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        photoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        if (r.getPhotoPath() != null && !r.getPhotoPath().isEmpty()) {
            try {
                Image img = Image.getInstance(r.getPhotoPath());
                img.scaleToFit(120, 120);
                img.setAlignment(Element.ALIGN_CENTER);
                photoCell.addElement(img);
            } catch (Exception e) { 
                // No photo - leave cell empty
            }
        }
        
        // Right Cell - Text Content
        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setBackgroundColor(theme);
        textCell.setPadding(25);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        
        Paragraph name = new Paragraph(r.getFullName(), headerNameFont);
        name.setSpacingAfter(8);
        textCell.addElement(name);
        
        if (r.getTitle() != null && !r.getTitle().isEmpty()) {
            Paragraph title = new Paragraph(r.getTitle(), headerTitleFont);
            title.setSpacingAfter(10);
            textCell.addElement(title);
        }
        
        Paragraph contact = new Paragraph();
        contact.add(new Chunk(r.getEmail() + "  •  ", headerContactFont));
        contact.add(new Chunk(r.getPhone() + "  •  ", headerContactFont));
        contact.add(new Chunk(r.getAddress(), headerContactFont));
        textCell.addElement(contact);
        
        headerTable.addCell(photoCell);
        headerTable.addCell(textCell);
        doc.add(headerTable);
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
        
        // Body Content
        // Summary
        if (r.getSummary() != null && !r.getSummary().isEmpty()) {
            addCreativeSection(doc, "ABOUT ME", sectionFont, theme);
            Paragraph summary = new Paragraph(r.getSummary(), normalFont);
            summary.setAlignment(Element.ALIGN_JUSTIFIED);
            summary.setSpacingAfter(15);
            doc.add(summary);
        }
        
        // Two-column for Education and Projects
        PdfPTable bottomTable = new PdfPTable(2);
        bottomTable.setWidthPercentage(100);
        bottomTable.setWidths(new float[]{50, 50});
        
        // Education Column
        PdfPCell eduCell = new PdfPCell();
        eduCell.setBorder(Rectangle.NO_BORDER);
        eduCell.setPaddingRight(10);
        
        if (!r.getEducationList().isEmpty()) {
            addCreativeSection(eduCell, "EDUCATION", sectionFont, theme);
            for (Education e : r.getEducationList()) {
                Paragraph degree = new Paragraph(e.getDegree(), boldFont);
                degree.setSpacingAfter(2);
                eduCell.addElement(degree);
                
                Paragraph uniInfo = new Paragraph();
                uniInfo.add(new Chunk(e.getUniversity(), smallFont));
                uniInfo.setSpacingAfter(5);
                eduCell.addElement(uniInfo);
                
                Paragraph cgpa = new Paragraph(e.getYear() + " | CGPA: " + e.getCgpa(), italicFont);
                cgpa.setSpacingAfter(12);
                eduCell.addElement(cgpa);
            }
        }

        
        
        // Skills Column
        PdfPCell skillsCell = new PdfPCell();
        skillsCell.setBorder(Rectangle.NO_BORDER);
        skillsCell.setPaddingLeft(10);
        
        if (!r.getSkillList().isEmpty()) {
            addCreativeSection(skillsCell, "SKILLS", sectionFont, theme);
            for (Skill s : r.getSkillList()) {
                Paragraph skill = new Paragraph("✓ " + s.getSkillName(), normalFont);
                skill.setSpacingAfter(4);
                skillsCell.addElement(skill);
            }
        }

        
        
        bottomTable.addCell(eduCell);
        bottomTable.addCell(skillsCell);
        doc.add(bottomTable);
        



        // Experience
        if (!r.getExperienceList().isEmpty()) {
            addCreativeSection(doc, "PROFESSIONAL EXPERIENCE", sectionFont, theme);
            for (Experience e : r.getExperienceList()) {
                PdfPTable expTable = new PdfPTable(1);
                expTable.setWidthPercentage(100);
                expTable.setSpacingAfter(10);
                
                PdfPCell expCell = new PdfPCell();
                expCell.setBorder(Rectangle.LEFT);
                expCell.setBorderColorLeft(theme);
                expCell.setBorderWidthLeft(3);
                expCell.setPadding(10);
                
                Paragraph role = new Paragraph(e.getRole(), boldFont);
                role.setSpacingAfter(2);
                expCell.addElement(role);
                
                Paragraph companyDuration = new Paragraph();
                companyDuration.add(new Chunk(e.getCompany() + " | ", normalFont));
                companyDuration.add(new Chunk(e.getDuration(), italicFont));
                companyDuration.setSpacingAfter(5);
                expCell.addElement(companyDuration);
                
                Paragraph desc = new Paragraph(e.getDescription(), normalFont);
                desc.setAlignment(Element.ALIGN_JUSTIFIED);
                expCell.addElement(desc);
                
                expTable.addCell(expCell);
                doc.add(expTable);
            }
        }
        


        // Projects
        if (!r.getProjectList().isEmpty()) {
            doc.add(Chunk.NEWLINE);
            addCreativeSection(doc, "PROJECTS", sectionFont, theme);
            for (Project p : r.getProjectList()) {
                Paragraph projectTitle = new Paragraph("◆ " + p.getTitle(), boldFont);
                projectTitle.setSpacingAfter(5);
                doc.add(projectTitle);
                
                Paragraph projectDesc = new Paragraph(p.getDescription(), normalFont);
                projectDesc.setAlignment(Element.ALIGN_JUSTIFIED);
                projectDesc.setSpacingAfter(12);
                doc.add(projectDesc);
            }
        }

        addCreativeCustomSections(doc, r, sectionFont, normalFont, theme);
    }

    private static void generateMinimalTemplate(Document doc, Resume r, Color theme, Color lightTheme,
            Font titleFont, Font subTitleFont, Font sectionFont, Font normalFont, Font boldFont, Font smallFont, Font italicFont)
            throws DocumentException, IOException {
        
        Font minimalName = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, Font.NORMAL, Color.BLACK);
        Font minimalTitle = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.ITALIC, theme);
        Font minimalContact = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
        Font minimalSection = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Font.NORMAL, theme);
        
        // Centered Header
        Paragraph name = new Paragraph(r.getFullName().toUpperCase(), minimalName);
        name.setAlignment(Element.ALIGN_CENTER);
        name.setSpacingAfter(5);
        doc.add(name);
        
        if (r.getTitle() != null && !r.getTitle().isEmpty()) {
            Paragraph title = new Paragraph(r.getTitle(), minimalTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(8);
            doc.add(title);
        }
        
        Paragraph contact = new Paragraph();
        contact.add(new Chunk(r.getEmail() + "  •  ", minimalContact));
        contact.add(new Chunk(r.getPhone() + "  •  ", minimalContact));
        contact.add(new Chunk(r.getAddress(), minimalContact));
        contact.setAlignment(Element.ALIGN_CENTER);
        contact.setSpacingAfter(12);
        doc.add(contact);
        
        LineSeparator line = new LineSeparator();
        line.setLineColor(Color.LIGHT_GRAY);
        doc.add(line);
        doc.add(Chunk.NEWLINE);
        
        // Content with minimal styling
        if (r.getSummary() != null && !r.getSummary().isEmpty()) {
            Paragraph summaryHead = new Paragraph("SUMMARY", minimalSection);
            summaryHead.setSpacingAfter(8);
            doc.add(summaryHead);
            
            Paragraph summary = new Paragraph(r.getSummary(), normalFont);
            summary.setAlignment(Element.ALIGN_JUSTIFIED);
            summary.setSpacingAfter(15);
            doc.add(summary);
        }
          if (!r.getSkillList().isEmpty()) {
            Paragraph skillsHead = new Paragraph("SKILLS", minimalSection);
            skillsHead.setSpacingAfter(8);
            doc.add(skillsHead);
            
            StringBuilder skills = new StringBuilder();
            for (int i = 0; i < r.getSkillList().size(); i++) {
                skills.append(r.getSkillList().get(i).getSkillName());
                if (i < r.getSkillList().size() - 1) skills.append("  •  ");
            }
            Paragraph skillsPara = new Paragraph(skills.toString(), normalFont);
            doc.add(skillsPara);
            doc.add(Chunk.NEWLINE);
        }

          if (!r.getEducationList().isEmpty()) {
            Paragraph eduHead = new Paragraph("EDUCATION", minimalSection);
            eduHead.setSpacingAfter(8);
            doc.add(eduHead);
            
            for (Education e : r.getEducationList()) {
                Paragraph degree = new Paragraph(e.getDegree() + "  —  " + e.getUniversity(), boldFont);
                degree.setSpacingAfter(2);
                doc.add(degree);
                
                Paragraph year = new Paragraph(e.getYear() + "  •  CGPA: " + e.getCgpa(), italicFont);
                year.setSpacingAfter(12);
                doc.add(year);
            }
            doc.add(Chunk.NEWLINE);
        }
        if (!r.getExperienceList().isEmpty()) {
            Paragraph expHead = new Paragraph("EXPERIENCE", minimalSection);
            expHead.setSpacingAfter(8);
            doc.add(expHead);
            
            for (Experience e : r.getExperienceList()) {
                Paragraph role = new Paragraph(e.getRole() + "  —  " + e.getCompany(), boldFont);
                role.setSpacingAfter(2);
                doc.add(role);
                
                Paragraph duration = new Paragraph(e.getDuration(), italicFont);
                duration.setSpacingAfter(5);
                doc.add(duration);
                
                Paragraph desc = new Paragraph(e.getDescription(), normalFont);
                desc.setAlignment(Element.ALIGN_JUSTIFIED);
                desc.setSpacingAfter(12);
                doc.add(desc);
            }
            doc.add(Chunk.NEWLINE);
        }
        
        if (!r.getEducationList().isEmpty()) {
            Paragraph eduHead = new Paragraph("EDUCATION", minimalSection);
            eduHead.setSpacingAfter(8);
            doc.add(eduHead);
            
            for (Education e : r.getEducationList()) {
                Paragraph degree = new Paragraph(e.getDegree() + "  —  " + e.getUniversity(), boldFont);
                degree.setSpacingAfter(2);
                doc.add(degree);
                
                Paragraph year = new Paragraph(e.getYear() + "  •  CGPA: " + e.getCgpa(), italicFont);
                year.setSpacingAfter(12);
                doc.add(year);
            }
            doc.add(Chunk.NEWLINE);
        }
        
        if (!r.getProjectList().isEmpty()) {
            Paragraph projHead = new Paragraph("PROJECTS", minimalSection);
            projHead.setSpacingAfter(8);
            doc.add(projHead);
            
            for (Project p : r.getProjectList()) {
                Paragraph projectTitle = new Paragraph(p.getTitle(), boldFont);
                projectTitle.setSpacingAfter(5);
                doc.add(projectTitle);
                
                Paragraph projectDesc = new Paragraph(p.getDescription(), normalFont);
                projectDesc.setAlignment(Element.ALIGN_JUSTIFIED);
                projectDesc.setSpacingAfter(12);
                doc.add(projectDesc);
            }
            doc.add(Chunk.NEWLINE);
        }

        addMinimalCustomSections(doc, r, minimalSection, normalFont);
    }

    // ==================== HELPER METHODS ====================

    private static void addEnhancedSection(Document doc, String title, Font font, Color theme) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        Paragraph p = new Paragraph(title, font);
        p.setSpacingAfter(5);
        doc.add(p);
        doc.add(new LineSeparator(2f, 100f, theme, Element.ALIGN_LEFT, -2));
        doc.add(Chunk.NEWLINE);
    }

    private static void addModernSection(PdfPCell cell, String title, Font font, Color theme) {
        Paragraph p = new Paragraph(title, font);
        p.setSpacingAfter(8);
        cell.addElement(p);
        
        LineSeparator line = new LineSeparator();
        line.setLineColor(theme);
        line.setLineWidth(2f);
        cell.addElement(new Chunk(line));
        cell.addElement(Chunk.NEWLINE);
    }

    private static void addCreativeSection(Document doc, String title, Font font, Color theme) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        Paragraph p = new Paragraph("▸ " + title, font);
        p.setSpacingAfter(8);
        doc.add(p);
    }

    private static void addCreativeSection(PdfPCell cell, String title, Font font, Color theme) {
        Paragraph p = new Paragraph("▸ " + title, font);
        p.setSpacingAfter(8);
        cell.addElement(p);
    }

    private static void addClassicCustomSections(Document doc, Resume resume, Font sectionFont, Font normalFont, Color theme)
            throws DocumentException {
        List<AbstractCustomSection> customSections = resume.getCustomSections();
        for (AbstractCustomSection section : customSections) {
            addEnhancedSection(doc, section.getHeader() + " (" + section.getOccupationName() + ")", sectionFont, theme);
            addCustomParameters(doc, section, normalFont);
        }
    }

    private static void addModernCustomSections(PdfPCell cell, Resume resume, Font sectionFont, Font normalFont, Color theme) {
        List<AbstractCustomSection> customSections = resume.getCustomSections();
        for (AbstractCustomSection section : customSections) {
            addModernSection(cell, section.getHeader() + " (" + section.getOccupationName() + ")", sectionFont, theme);
            addCustomParameters(cell, section, normalFont);
        }
    }

    private static void addCreativeCustomSections(Document doc, Resume resume, Font sectionFont, Font normalFont, Color theme)
            throws DocumentException {
        List<AbstractCustomSection> customSections = resume.getCustomSections();
        for (AbstractCustomSection section : customSections) {
            addCreativeSection(doc, section.getHeader() + " (" + section.getOccupationName() + ")", sectionFont, theme);
            addCustomParameters(doc, section, normalFont);
        }
    }

    private static void addMinimalCustomSections(Document doc, Resume resume, Font sectionFont, Font normalFont)
            throws DocumentException {
        List<AbstractCustomSection> customSections = resume.getCustomSections();
        for (AbstractCustomSection section : customSections) {
            Paragraph customHead = new Paragraph(section.getHeader().toUpperCase() + " (" +
                    section.getOccupationName().toUpperCase() + ")", sectionFont);
            customHead.setSpacingAfter(8);
            doc.add(customHead);
            addCustomParameters(doc, section, normalFont);
            doc.add(Chunk.NEWLINE);
        }
    }

    private static void addCustomParameters(Document doc, AbstractCustomSection section, Font normalFont)
            throws DocumentException {
        for (CustomParameter parameter : section.getParameters()) {
            Paragraph line = new Paragraph(parameter.getName() + " : " + parameter.getValue(), normalFont);
            line.setSpacingAfter(4);
            doc.add(line);
        }
    }

    private static void addCustomParameters(PdfPCell cell, AbstractCustomSection section, Font normalFont) {
        for (CustomParameter parameter : section.getParameters()) {
            Paragraph line = new Paragraph(parameter.getName() + " : " + parameter.getValue(), normalFont);
            line.setSpacingAfter(4);
            cell.addElement(line);
        }
    }

    // Page Numbers
    static class PageNumbersInfo extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Phrase footer = new Phrase("Page " + writer.getPageNumber(), 
                FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, Color.GRAY));
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer, 
                (document.right() - document.left()) / 2 + document.leftMargin(), 
                document.bottom() - 10, 0);
        }
    }
}
