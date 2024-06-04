package org.swamedia.mediators;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONException;
import org.json.JSONObject;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;

public class JSONToPdf extends AbstractMediator {

	public static String generateIdentityCard(JSONObject jsonObject) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<!DOCTYPE html>\n");
		stringBuilder.append("<html lang=\"en\">\n");
		stringBuilder.append("<head>\n");
		stringBuilder.append("    <meta charset=\"UTF-8\">\n");
		stringBuilder.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
		stringBuilder.append("    <title>Indonesian ID Card</title>\n");
		stringBuilder.append("    <style>\n");
		stringBuilder.append("        body {\n");
		stringBuilder.append("            font-family: Arial, sans-serif;\n");
		stringBuilder.append("            background-color: #f0f0f0;\n");
		stringBuilder.append("            display: flex;\n");
		stringBuilder.append("            justify-content: center;\n");
		stringBuilder.append("            align-items: center;\n");
		stringBuilder.append("            height: 100vh;\n");
		stringBuilder.append("            margin: 0;\n");
		stringBuilder.append("        }\n");
		stringBuilder.append("        .id-card {\n");
		stringBuilder.append("            width: 300px;\n");
		stringBuilder.append("            background-color: #fff;\n");
		stringBuilder.append("            border-radius: 10px;\n");
		stringBuilder.append("            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);\n");
		stringBuilder.append("            padding: 20px;\n");
		stringBuilder.append("            text-align: center;\n");
		stringBuilder.append("        }\n");
		stringBuilder.append("        .photo {\n");
		stringBuilder.append("            width: 100%;\n");
		stringBuilder.append("            max-width: 100%;\n");
		stringBuilder.append("            max-height: 100%;\n");
		stringBuilder.append("            height: auto; /* This ensures the image maintains its aspect ratio */\n");
		stringBuilder.append("            margin: 0 auto 10px;\n");
		stringBuilder.append("        }\n");
		stringBuilder.append("        .info {\n");
		stringBuilder.append("            font-size: 16px;\n");
		stringBuilder.append("            margin: 10px 0;\n");
		stringBuilder.append("        }\n");
		stringBuilder.append("    </style>\n");
		stringBuilder.append("</head>\n");
		stringBuilder.append("<body>\n");
		stringBuilder.append("    <div class=\"id-card\">\n");
		stringBuilder.append("        <div class=\"photo\">\n");
		stringBuilder.append(
				"            <img class=\"photo\" src=\"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTC5VmTpI56WYZPDMfSc77LU0KuV6lmm7PomW8ItCsMcbKuaabuj8bgDYmZtxKXCruVmEI&usqp=CAU\" alt=\"\"/>\n");
		stringBuilder.append("        </div>\n");
		stringBuilder.append("        <div>\n");
		Iterator<String> keys = jsonObject.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = jsonObject.get(key);
			stringBuilder.append("<div class=\\\"info\\\">").append(key).append(" : ").append(value.toString())
					.append("</div>");
		}
		stringBuilder.append("        </div>\n");
		stringBuilder.append("    </div>\n");
		stringBuilder.append("</body>\n");
		stringBuilder.append("</html>\n");
		return stringBuilder.toString();
	}

	public static byte[] convertHtmlToPdfInMemory(String htmlContent) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ConverterProperties converterProperties = new ConverterProperties();
		HtmlConverter.convertToPdf(htmlContent, outputStream, converterProperties);
		return outputStream.toByteArray();
	}

	@Override
	public boolean mediate(org.apache.synapse.MessageContext synCtx) {
		MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
		try {
			JSONObject jsonObject = new JSONObject(JsonUtil.jsonPayloadToString(axis2MessageContext));
			byte[] pdfBytes = convertHtmlToPdfInMemory(generateIdentityCard(jsonObject));
			String filePath = jsonObject.has("saved_location")
					? (jsonObject.getString("saved_location") + "/" + (new Date()).getTime() + ".pdf")
					: "/home/lukman/WSO2/Enterprise Integrator Project/file/default.pdf";
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(pdfBytes);
			fos.close();
			String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);
			JsonUtil.removeJsonPayload(axis2MessageContext);
			jsonObject = new JSONObject();
			jsonObject.put("file_location", filePath);
			jsonObject.put("base64", pdfBase64);
			JsonUtil.getNewJsonPayload(axis2MessageContext, jsonObject.toString(), true, true);
			this.log.info("File pdf is saved to : " + filePath);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}
