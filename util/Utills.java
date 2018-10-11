package com.coin.app.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;

import com.coin.app.model.User;
import com.coin.app.repository.UserRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import ir.huri.jcal.JalaliCalendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class Utills
{
    @Value("${app.photpCal.directory}")
    private String path;


    public static String toFakePrice(String price, boolean bid)
    {
        int suffix = price.split("\\.")[1].length();
        double amount = Double.parseDouble(price);
        StringBuilder pattern = new StringBuilder("#,###.");

        for (int i = 0; i < suffix; i++)
        {
            amount = amount * 10;
            pattern.append("#");
        }

        if (bid)
            amount = amount - (0.01 * amount);
        else
            amount = amount + (0.01 * amount);
        amount = Math.round(amount);

        for (int i = 0; i < suffix; i++)
            amount = amount / 10;

        DecimalFormat formatter = new DecimalFormat(pattern.toString());

        return formatter.format(amount);
    }

    public static String commaSeparator(String value)
    {
        double amount = Double.parseDouble(value);
        DecimalFormat formatter = new DecimalFormat("#,###.#########");
        return formatter.format(amount);
    }

    public static String nameDisplayForDate(String date, boolean includeYear)
    {
        String year = date.split("-")[0].substring(2, 4);
        int month = Integer.valueOf(date.split("-")[1]);
        String day = date.split("-")[2];
        return day + " " + JalaliMonths.values()[month - 1] + " " + (includeYear ? year : "");
    }

    public static String nameDisplayForDate(LocalDate localDate, boolean includeYear)
    {
        return nameDisplayForDate(new JalaliCalendar(localDate).toString(), includeYear);
    }

    public static String shortDisplayForTime(String time)
    {
        return time.split(":")[0] + ":" + time.split(":")[1];
    }

    public static String addLeadingZeros(int count, long num, boolean split)
    {
        String str = String.format("%0" + (count + 1) + "d", num);
        String part1 = str.substring(0, (count + 1) / 2);
        String part2 = str.substring((count + 1) / 2, str.length());
        return part2 + (split ? "-" : "") + part1;
    }

    public static String createPhotoCalPDF(Long formTemplateId, List<User> userList)
    {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String fileName = "test.pdf";

//        try
//        {
//            PdfPTable table = new PdfPTable(3);
//            table.setWidthPercentage(100);
//            table.setWidths(new int[]{1, 3, 3});
//
//            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
//
//            PdfPCell hcell;
//            hcell = new PdfPCell(new Phrase("Id", headFont));
//            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//            table.addCell(hcell);
//
//            hcell = new PdfPCell(new Phrase("Username", headFont));
//            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//            table.addCell(hcell);
//
//            hcell = new PdfPCell(new Phrase("Email", headFont));
//            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//            table.addCell(hcell);
//
//            for (User user : userList)
//            {
//                PdfPCell cell;
//
//                cell = new PdfPCell(new Phrase(user.getId().toString()));
//                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                table.addCell(cell);
//
//                cell = new PdfPCell(new Phrase(user.getUsername()));
//                cell.setPaddingLeft(5);
//                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
//                table.addCell(cell);
//
//                cell = new PdfPCell(new Phrase(String.valueOf(user.getEmail())));
//                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//                cell.setPaddingRight(5);
//                table.addCell(cell);
//            }
//
//            PdfWriter.getInstance(document, out);
//            document.open();
//            document.add(table);
//
//            document.close();
//
//            File file = new File("C://Users/javad.farzaneh/projects/Examples/CoinProjectClient/src/assets/photoCalendar/" + fileName);
//
//            FileOutputStream fos = null;
//            try
//            {
//                fos = new FileOutputStream(file);
//                // Writes bytes from the specified byte array to this file output stream
//                fos.write(out.toByteArray());
//
//            } catch (FileNotFoundException e)
//            {
//                System.out.println("File not found" + e);
//            } catch (IOException ioe)
//            {
//                System.out.println("Exception while writing file " + ioe);
//            } finally
//            {
//                // close the streams using close method
//                try
//                {
//                    if (fos != null)
//                        fos.close();
//                } catch (IOException ioe)
//                {
//                    System.out.println("Error while closing stream: " + ioe);
//                }
//            }
//
//        } catch (DocumentException ex)
//        {
//            System.out.println("DocumentException : " + ex.getMessage());
//        }

        return fileName;
    }
}
