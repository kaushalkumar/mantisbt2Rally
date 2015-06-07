
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;

import org.apache.commons.codec.binary.Base64;
import org.cyberneko.html.parsers.DOMParser;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.DOMReader;
import org.xml.sax.InputSource;

import biz.futureware.mantis.rpc.soap.client.MantisConnectLocator;
import biz.futureware.mantis.rpc.soap.client.MantisConnectPortType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.QueryFilter;
import com.rallydev.rest.util.Ref;

public class MantisBTDefectsToRally {

	static Map<String, String> userRefMap = new HashMap<String, String>();

	public static void main(String[] args) throws Exception {
		DOMParser parser = new DOMParser();

		parser.parse(new InputSource("MantisBT_Sample_report.html"));
		org.w3c.dom.Document doc = parser.getDocument();

		DOMReader reader = new DOMReader();
		Document document = reader.read(doc);

		@SuppressWarnings("unchecked")
		XPath xPath = DocumentHelper.createXPath("//TABLE[@class='width100']");

		@SuppressWarnings("unchecked")
		List<Node> selectNodes = xPath.selectNodes(document);
		System.out.println("Number of nodes: " + selectNodes.size());

		MantisConnectPortType portType = new MantisConnectLocator().getMantisConnectPort(new URL("http://<<<HOST:PORT>>>/mantisbt/api/soap/mantisconnect.php"));
		RallyRestApi restApi = new RallyRestApi(new URI("https://rally1.rallydev.com"), "<<<API_KEY>>>");

		// release ref
		QueryRequest releaseRequest = new QueryRequest("Release");
		releaseRequest.setQueryFilter(new QueryFilter("Name", "=", "<<<RELEASE_NAME)IN_RALLY>>>"));
		QueryResponse releaseQueryResponse = restApi.query(releaseRequest);
		JsonArray releaseQueryResults = releaseQueryResponse.getResults();
		JsonElement releaseQueryElement = releaseQueryResults.get(0);
		JsonObject releaseQueryObject = releaseQueryElement.getAsJsonObject();
		String releaseRef = releaseQueryObject.get("_ref").getAsString();
		
		
		for (int i = 0; i < selectNodes.size(); i++) {
			Node node = (Node) (selectNodes.get(i));
			i++;
			Node notesNode = (Node) (selectNodes.get(i));
			convertDataToDefect(node, notesNode, restApi, portType, releaseRef);
		}

	}

	private static void convertDataToDefect(Node defectDataNode, Node notesNode, RallyRestApi restApi, MantisConnectPortType portType, String releaseRef) throws Exception {
		
		JsonObject newDefect = new JsonObject();

		XPath xPath = DocumentHelper.createXPath("TBODY/TR[@class='print']/TD[@class='print']");
		List<?> selectNodes = xPath.selectNodes(defectDataNode);

		Node data = (Node) selectNodes.get(0);
		String id = data.getStringValue().trim();

		data = (Node) selectNodes.get(1);
		String category = data.getStringValue().trim();

		data = (Node) selectNodes.get(2);
		String severity = data.getStringValue().trim();

		data = (Node) selectNodes.get(3);
		String reproducability = data.getStringValue().trim();

		data = (Node) selectNodes.get(4);
		String dateSubmitted = data.getStringValue().trim();

		data = (Node) selectNodes.get(5);
		String lastUpdated = data.getStringValue().trim();

		data = (Node) selectNodes.get(6);
		String reporter = data.getStringValue().trim();

		data = (Node) selectNodes.get(7);
		String platform = data.getStringValue().trim();

		data = (Node) selectNodes.get(9);
		String assignedTo = data.getStringValue().trim();

		data = (Node) selectNodes.get(10);
		String os = data.getStringValue().trim();

		data = (Node) selectNodes.get(12);
		String priority = data.getStringValue().trim();

		data = (Node) selectNodes.get(13);
		String osv = data.getStringValue().trim();

		data = (Node) selectNodes.get(15);
		String status = data.getStringValue().trim();

		data = (Node) selectNodes.get(16);
		String prodV = data.getStringValue().trim();

		data = (Node) selectNodes.get(18);
		String prodB = data.getStringValue().trim();

		data = (Node) selectNodes.get(19);
		String resolution = data.getStringValue().trim();

		data = (Node) selectNodes.get(21);
		String projection = data.getStringValue().trim();

		data = (Node) selectNodes.get(24);
		String eta = data.getStringValue().trim();

		data = (Node) selectNodes.get(25);
		String fixedInVersion = data.getStringValue().trim();

		data = (Node) selectNodes.get(28);
		String targetVersion = data.getStringValue().trim();

		data = (Node) selectNodes.get(30);
		String iteration = data.getStringValue().trim();

		data = (Node) selectNodes.get(31);
		String product = data.getStringValue().trim();

		data = (Node) selectNodes.get(32);
		String region = data.getStringValue().trim();

		data = (Node) selectNodes.get(33);
		String rootCause = data.getStringValue().trim();

		data = (Node) selectNodes.get(34);
		String stage = data.getStringValue().trim();

		data = (Node) selectNodes.get(35);
		String testCycle = data.getStringValue().trim();

		data = (Node) selectNodes.get(36);
		String summary = data.getStringValue().trim();

		data = (Node) selectNodes.get(37);
		String description = data.getStringValue().trim();

		data = (Node) selectNodes.get(38);
		String stepsToReproduce = data.getStringValue().trim();

		data = (Node) selectNodes.get(39);
		String additionalInfo = data.getStringValue().trim();

		List<Attachment> attachmentDataList = new ArrayList<Attachment>();

		data = (Node) selectNodes.get(40);
		String attachmentData = data.getStringValue().trim();
		// String pattern = "http://172.24.14.173(.*)type=bug";
		//
		String replacelinebrkAll1 = attachmentData.replaceAll("\\n", "");
		String[] split = replacelinebrkAll1.split("&type=bug");

		for (String string : split) {
			String[] splittedDataString = string.split(" \\((.*)file_id=");
			if (splittedDataString.length > 1) {
				Attachment attachment = new Attachment();
				attachment.setAttachmentName(splittedDataString[0]);

				byte[] mc_issue_attachment_get = portType.mc_issue_attachment_get("<<<MANTIS_USER_ID>>>", "<<<MANTIS_PASSWORD>>>", BigInteger.valueOf(Integer.valueOf(splittedDataString[1])));
				attachment.setAttachmentData(mc_issue_attachment_get);
				attachmentDataList.add(attachment);
			}
		}

		XPath xPathNotes = DocumentHelper.createXPath("TBODY/TR/TD[@class='print']");
		List<?> notesData = xPathNotes.selectNodes(notesNode);
		StringBuffer note = new StringBuffer();
		note.append("Mantis ID: " + id + "<br/>");
		note.append("Reproducability: " + reproducability + "<br/>");
		note.append("Date Submitted: " + dateSubmitted + "<br/>");
		note.append("Date Last Updated: " + lastUpdated + "<br/>");

		if (notesData.size() > 0) {
			// There are no notes attached to this issue.
			data = (Node) notesData.get(0);
			note.append(data.getText().trim());
		} else {
			xPathNotes = DocumentHelper.createXPath("TBODY/TR/TD[@class='nopad' and @width='20%']/..");
			notesData = xPathNotes.selectNodes(notesNode);
			for (int i = 0; i < notesData.size(); i++) {
				if (i != 0) {
					note.append("<br/>");
				}
				data = (Node) notesData.get(i);
				XPath userNoteXPath = DocumentHelper.createXPath("TD[@class='nopad' and @width='20%']/TABLE[@class='hide']/TBODY/TR/TD[@class='print']/A");
				XPath userDateXPath = DocumentHelper.createXPath("(TD[@class='nopad' and @width='20%']/TABLE[@class='hide']/TBODY/TR/TD[@class='print'])[3]");
				XPath noteXPath = DocumentHelper.createXPath("TD[@class='nopad' and @width='85%']/TABLE[@class='hide']/TBODY/TR/TD[@class='print']");
				List<?> selectNodes2 = userNoteXPath.selectNodes(data);
				String user = ((Node) selectNodes2.get(0)).getStringValue().trim();
				List<?> selectNodes3 = userDateXPath.selectNodes(data);
				String date = ((Node) selectNodes3.get(0)).getStringValue().trim();
				List<?> selectNodes4 = noteXPath.selectNodes(data);
				String noteValue = ((Node) selectNodes4.get(0)).getStringValue().trim();
				note.append("" + user + "[" + date + "]: " + noteValue);
			}
		}

		newDefect.addProperty("Name", summary);
		newDefect.addProperty("Description", description + "<br/>" + stepsToReproduce + "<br/>" + additionalInfo);
		newDefect.addProperty("Severity", getSeverity(severity));
		newDefect.addProperty("Priority", getPriority(priority));

		newDefect.addProperty("State", getDefectState(status));
		newDefect.addProperty("ScheduleState", getScheduleState(status));
		newDefect.addProperty("Resolution", getResolution(resolution));
		newDefect.addProperty("Notes", note.toString());

		newDefect.addProperty("Release", releaseRef);

		newDefect.addProperty("SubmittedBy", getUser(reporter, restApi));
		newDefect.addProperty("Owner", getUser(assignedTo, restApi));

		CreateRequest createRequest = new CreateRequest("defect", newDefect);

		CreateResponse createResponse = restApi.create(createRequest);

		if (createResponse.wasSuccessful()) {

			System.out.println(String.format("Created %s", createResponse.getObject().get("_ref").getAsString()));

			// Read defect
			String ref = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
			System.out.println(String.format("\nReading Defect %s...", ref));

			String imageBase64String;
			int attachmentSize;
			String mimeType;

			if (attachmentDataList.size() > 0) {
				for (Attachment attachment : attachmentDataList) {

					try {
						imageBase64String = Base64.encodeBase64String(attachment.getAttachmentData());
						attachmentSize = attachment.getAttachmentData().length;

						MagicMatch match = Magic.getMagicMatch(attachment.getAttachmentData());
						mimeType = match.getMimeType();

						// First create AttachmentContent
						JsonObject myAttachmentContent = new JsonObject();
						myAttachmentContent.addProperty("Content", imageBase64String);
						CreateRequest attachmentContentCreateRequest = new CreateRequest("AttachmentContent", myAttachmentContent);
						CreateResponse attachmentContentResponse = restApi.create(attachmentContentCreateRequest);
						String myAttachmentContentRef = attachmentContentResponse.getObject().get("_ref").getAsString();
						System.out.println("Attachment Content created: " + myAttachmentContentRef);

						// Now create the Attachment itself
						JsonObject myAttachment = new JsonObject();
						myAttachment.addProperty("Artifact", ref);
						myAttachment.addProperty("Content", myAttachmentContentRef);
						myAttachment.addProperty("Name", attachment.getAttachmentName());
						myAttachment.addProperty("Description", attachment.getAttachmentName());
						myAttachment.addProperty("ContentType", mimeType);
						myAttachment.addProperty("Size", attachmentSize);

						CreateRequest attachmentCreateRequest = new CreateRequest("Attachment", myAttachment);
						CreateResponse attachmentResponse = restApi.create(attachmentCreateRequest);
						String myAttachmentRef = attachmentResponse.getObject().get("_ref").getAsString();
						System.out.println("Attachment  created: " + myAttachmentRef);

						if (attachmentResponse.wasSuccessful()) {
							System.out.println("Successfully created Attachment");
						} else {
							String[] attachmentContentErrors;
							attachmentContentErrors = attachmentResponse.getErrors();
							System.out.println("Error occurred creating Attachment: ");
							for (int j = 0; j < attachmentContentErrors.length; j++) {
								System.out.println(attachmentContentErrors[j]);
							}
						}
					} catch (Exception e) {
						System.out.println("Exception occurred while attempting to create Content and/or Attachment: ");
						e.printStackTrace();
					}
				}
			}
		} else {
			String[] createErrors;
			createErrors = createResponse.getErrors();
			System.out.println("Error occurred creating a defect: ");
			for (int j = 0; j < createErrors.length; j++) {
				System.out.println(createErrors[j]);
			}
		}
	}

	private static String getSeverity(String severityInput) {
		Map<String, String> severityMap = new HashMap<String, String>();
		severityMap.put("block", "Crash/Data Loss");
		severityMap.put("crash", "Crash/Data Loss");
		severityMap.put("major", "Major Problem");
		severityMap.put("minor", "Minor Problem");
		return severityMap.get(severityInput);
	}

	private static String getPriority(String priorityInput) {
		Map<String, String> priorityMap = new HashMap<String, String>();
		priorityMap.put("immediate", "Resolve Immediately");
		priorityMap.put("urgent", "Resolve Immediately");
		priorityMap.put("high", "High Attention");
		priorityMap.put("normal", "Normal");
		priorityMap.put("low", "Low");
		return priorityMap.get(priorityInput);
	}

	private static String getUser(String userInput, RallyRestApi restApi) throws IOException {
		Map<String, String> userMap = new HashMap<String, String>();
		userMap.put("<<<USER1_IN_MANTIS>>>", "<<<USER1_IN_RALLY>>>");
		userMap.put("<<<USER2_IN_MANTIS>>>", "<<<USER2_IN_RALLY>>>");
		userMap.put("<<<USER3_IN_MANTIS>>>", "<<<USER3_IN_RALLY>>>");
		userMap.put("<<<USER4_IN_MANTIS>>>", "<<<USER4_IN_RALLY>>>");

		if (userRefMap.get(userInput) == null) {
			// Read User
			QueryRequest userRequest = new QueryRequest("User");
			userRequest.setQueryFilter(new QueryFilter("DisplayName", "=", userMap.get(userInput)));
			QueryResponse userQueryResponse = restApi.query(userRequest);
			JsonArray userQueryResults = userQueryResponse.getResults();
			JsonElement userQueryElement = userQueryResults.get(0);
			JsonObject userQueryObject = userQueryElement.getAsJsonObject();
			String userRef = userQueryObject.get("_ref").getAsString();

			userRefMap.put(userInput, userRef);
		}
		return userRefMap.get(userInput);
	}

	private static String getDefectState(String defectStateInput) {
		Map<String, String> defectStateMap = new HashMap<String, String>();
		defectStateMap.put("", "Submitted");
		defectStateMap.put("assigned", "Open");
		defectStateMap.put("awaiting information", "Open");
		defectStateMap.put("deferred", "Open");
		defectStateMap.put("feedback", "Open");
		defectStateMap.put("rejected", "Open");
		defectStateMap.put("reopen", "Open");
		defectStateMap.put("resolved", "Open");
		defectStateMap.put("intest", "Fixed");
		defectStateMap.put("ready to test", "Fixed");
		defectStateMap.put("closed", "Closed");
		return defectStateMap.get(defectStateInput);
	}

	private static String getResolution(String resolutionInput) {
		Map<String, String> resolutionMap = new HashMap<String, String>();
		resolutionMap.put("duplicate", "Duplicate");
		resolutionMap.put("fixed", "Code Change");
		resolutionMap.put("no change required", "Not a Defect");
		resolutionMap.put("open", "");
		resolutionMap.put("suspended", "");
		resolutionMap.put("unable to reproduce", "Not a Defect");
		resolutionMap.put("won't fix", "");
		return resolutionMap.get(resolutionInput);
	}

	private static String getScheduleState(String defectStateInput) {
		Map<String, String> scheduleStateMap = new HashMap<String, String>();
		scheduleStateMap.put("", "Defined");
		scheduleStateMap.put("assigned", "In-Progress");
		scheduleStateMap.put("awaiting information", "In-Progress");
		scheduleStateMap.put("deferred", "In-Progress");
		scheduleStateMap.put("feedback", "In-Progress");
		scheduleStateMap.put("rejected", "In-Progress");
		scheduleStateMap.put("reopen", "In-Progress");
		scheduleStateMap.put("resolved", "Completed");
		scheduleStateMap.put("intest", "Completed");
		scheduleStateMap.put("ready to test", "Completed");
		scheduleStateMap.put("closed", "Accepted");
		return scheduleStateMap.get(defectStateInput);
	}
}

class Attachment {
	private byte[] attachmentData;
	private String attachmentName;

	/**
	 * @return the attachmentData
	 */
	public byte[] getAttachmentData() {
		return attachmentData;
	}

	/**
	 * @param attachmentData
	 *            the attachmentData to set
	 */
	public void setAttachmentData(byte[] attachmentData) {
		this.attachmentData = attachmentData;
	}

	/**
	 * @return the attachmentName
	 */
	public String getAttachmentName() {
		return attachmentName;
	}

	/**
	 * @param attachmentName
	 *            the attachmentName to set
	 */
	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

}
