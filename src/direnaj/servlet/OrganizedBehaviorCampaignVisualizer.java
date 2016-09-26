package direnaj.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.util.CollectionUtil;
import direnaj.util.DateTimeUtils;
import direnaj.util.NumberUtils;
import direnaj.util.TextUtils;

public class OrganizedBehaviorCampaignVisualizer extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7542468189990963215L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json; charset=UTF-8");
		String requestType = req.getParameter("requestType");
		String jsonStr = "{}";

		// check for type
		try {
			JSONArray jsonArray = new JSONArray();
			String requestId = req.getParameter("requestId");
			BasicDBObject query = new BasicDBObject("requestId", requestId);
			BasicDBObject query4CosSimilarityRequest = new BasicDBObject(
					MongoCollectionFieldNames.MONGO_COS_SIM_REQ_ORG_REQUEST_ID, requestId);
			query4CosSimilarityRequest.put(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT,
					new BasicDBObject("$gt", 5));
			if ("visualizeUserCreationTimes".equals(requestType)) {
				visualizeUserCreationTimes(jsonArray, query);
			} else if ("visualizeUserTweetEntityRatios".equals(requestType)) {
				visualizeUserTweetEntityRatios(jsonArray, query);
			} else if ("visualizeUserFriendFollowerRatio".equals(requestType)) {
				visualizeUserFriendFollowerRatio(jsonArray, query);
			} else if ("visualizeUserRoughHashtagTweetCounts".equals(requestType)) {
				visualizeUserRoughHashtagTweetCounts(jsonArray, query);
			} else if ("visualizeUserPostDeviceRatios".equals(requestType)) {
				visualizeUserPostDeviceRatios(jsonArray, query);
			} else if ("visualizeUserRoughTweetCountsInBarChart".equals(requestType)) {
				visualizeUserRoughTweetCountsInBarChart(jsonArray, query);
			} else if ("visualizeHourlyUserAndTweetCount".equals(requestType)) {
				query4CosSimilarityRequest.remove(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT);
				visualizeHourlyUserAndTweetCount(jsonArray, query4CosSimilarityRequest);
			} else if ("visualizeHourlyTweetSimilarities".equals(requestType)) {
				visualizeHourlyTweetSimilarities(jsonArray, query4CosSimilarityRequest);
			} else if ("visualizeUserCreationTimesInBarChart".equals(requestType)) {
				visualizeUserCreationTimesInBarChart(jsonArray, query);
			} else if ("visualizeUserFriendFollowerRatioInBarChart".equals(requestType)) {
				visualizeUserFriendFollowerRatioInBarChart(jsonArray, query, requestId);
			} else if ("visualizeUserRoughHashtagTweetCountsInBarChart".equals(requestType)) {
				visualizeUserRoughHashtagTweetCountsInBarChart(jsonArray, query, requestId);
			} else if ("visualizeUserTweetEntityRatiosInBarChart".equals(requestType)) {
				visualizeUserTweetEntityRatiosInBarChart(jsonArray, query);
			} else if ("visualizeHourlyEntityRatios".equals(requestType)) {
				visualizeHourlyEntityRatios(jsonArray, query4CosSimilarityRequest);
			} else if ("visualizeHourlyRetweetRatios".equals(requestType)) {
				visualizeHourlyTweetRatios(jsonArray, query4CosSimilarityRequest);
			} else if (("getMeanVariance".equals(requestType))) {
				getMeanVariance4All(jsonArray, query, query4CosSimilarityRequest, requestId);
			}

			// FIXME 20160813 Sil
			jsonStr = jsonArray.toString();
			// System.out.println("Request Type : " + requestType);
			// System.out.println("Returned String : " + jsonStr);
		} catch (JSONException e) {
			Logger.getLogger(MongoPaginationServlet.class)
					.error("Error in OrganizedBehaviorCampaignVisualizer Servlet.", e);
		} catch (Exception e) {
			Logger.getLogger(MongoPaginationServlet.class)
					.error("Error in OrganizedBehaviorCampaignVisualizer Servlet.", e);
		}

		// return result
		PrintWriter printout = resp.getWriter();
		printout.println(jsonStr);
	}

	private void visualizeUserRoughHashtagTweetCountsInBarChart(JSONArray jsonArray, BasicDBObject query,
			String requestId) throws JSONException {
		List<String> limits = new ArrayList<>();
		limits.add("1");
		limits.add("2");
		limits.add("3-5");
		limits.add("6-10");
		limits.add("11-20");
		limits.add("21-50");
		limits.add("51-100");
		limits.add("100-200");
		limits.add("200-...");
		Map<String, Double> rangePercentages = new HashMap<>();
		for (String limit : limits) {
			rangePercentages.put(limit, 0d);
		}

		// get cursor
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query);
		// get objects from cursor
		int userCount = 0;
		while (paginatedResult.hasNext()) {
			userCount++;
			DBObject next = paginatedResult.next();
			double userHashtagPostCount = NumberUtils.roundDouble(4,
					(double) next.get(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT));
			CollectionUtil.findGenericRange(limits, rangePercentages, userHashtagPostCount);
		}
		CollectionUtil.calculatePercentage(rangePercentages, userCount);
		for (String limit : limits) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ratio", limit);
			jsonObject.put("percentage", rangePercentages.get(limit));
			jsonArray.put(jsonObject);
		}
	}

	private void visualizeUserTweetEntityRatiosInBarChart(JSONArray jsonArray, BasicDBObject query)
			throws JSONException {
		Map<String, Map<String, Double>> ratioValues = new HashMap<>();
		// define limits
		List<String> limits = new ArrayList<>();
		limits.add("0");
		limits.add("0.0001-0.5");
		limits.add("0.6-0.9");
		for (int i = 1; i <= 20; i++) {
			limits.add(String.valueOf(i));

		}
		limits.add("21-...");
		// init hash map
		for (String limit : limits) {
			// range percentages
			Map<String, Double> rangePercentages = new HashMap<>();
			rangePercentages.put(MongoCollectionFieldNames.MONGO_URL_RATIO, 0d);
			rangePercentages.put(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, 0d);
			rangePercentages.put(MongoCollectionFieldNames.MONGO_MENTION_RATIO, 0d);
			// add to ratio values
			ratioValues.put(limit, rangePercentages);
		}

		// get cursor
		DBCursor processInputResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query);

		// get objects from cursor
		// get url ratio
		int userCount = 0;
		while (processInputResult.hasNext()) {
			userCount++;
			DBObject next = processInputResult.next();
			// url ratio
			double urlRatio = NumberUtils.roundDouble(1, (double) next.get(MongoCollectionFieldNames.MONGO_URL_RATIO));
			if (urlRatio > 1d) {
				urlRatio = NumberUtils.roundDouble(0, urlRatio);
			}
			// hashtag ratio
			double hashtagRatio = NumberUtils.roundDouble(1,
					(double) next.get(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO));
			if (hashtagRatio > 1d) {
				hashtagRatio = NumberUtils.roundDouble(0, hashtagRatio);
			}
			// mention ratio
			double mentionRatio = NumberUtils.roundDouble(1,
					(double) next.get(MongoCollectionFieldNames.MONGO_MENTION_RATIO));
			if (mentionRatio > 1d) {
				mentionRatio = NumberUtils.roundDouble(0, mentionRatio);
			}
			// get range
			CollectionUtil.findGenericRange(limits, ratioValues, MongoCollectionFieldNames.MONGO_URL_RATIO, urlRatio);
			CollectionUtil.findGenericRange(limits, ratioValues, MongoCollectionFieldNames.MONGO_HASHTAG_RATIO,
					hashtagRatio);
			CollectionUtil.findGenericRange(limits, ratioValues, MongoCollectionFieldNames.MONGO_MENTION_RATIO,
					mentionRatio);
		}

		CollectionUtil.calculatePercentageForNestedMap(ratioValues, userCount);
		for (String limit : limits) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ratio", limit);
			jsonObject.put(MongoCollectionFieldNames.MONGO_URL_RATIO,
					ratioValues.get(limit).get(MongoCollectionFieldNames.MONGO_URL_RATIO));
			jsonObject.put(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO,
					ratioValues.get(limit).get(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO));
			jsonObject.put(MongoCollectionFieldNames.MONGO_MENTION_RATIO,
					ratioValues.get(limit).get(MongoCollectionFieldNames.MONGO_MENTION_RATIO));
			jsonArray.put(jsonObject);
		}
	}

	private void visualizeUserFriendFollowerRatioInBarChart(JSONArray jsonArray, BasicDBObject query, String requestId)
			throws JSONException {
		// get cursor
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query);

		// get objects from cursor
		int userCount = 0;
		Map<Double, Double> ratioPercentages = new HashMap<>();
		while (paginatedResult.hasNext()) {
			userCount++;
			DBObject next = paginatedResult.next();
			double friendFolloweRatio = NumberUtils.roundDouble(1,
					(double) next.get(MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO));
			CollectionUtil.incrementKeyValueInMap(ratioPercentages, friendFolloweRatio);
		}
		CollectionUtil.calculatePercentage(ratioPercentages, userCount);
		ratioPercentages = CollectionUtil.sortByComparator4Key(ratioPercentages);

		for (Entry<Double, Double> entry : ratioPercentages.entrySet()) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ratio", entry.getKey());
			jsonObject.put("percentage", entry.getValue());
			jsonArray.put(jsonObject);
		}

	}

	private void getMeanVariance4All(JSONArray jsonArray, BasicDBObject query, BasicDBObject query4CosSimilarityRequest,
			String requestId) throws Exception {

		Logger.getLogger(MongoPaginationServlet.class)
				.debug("getMeanVariance4All is started for requestId : " + requestId);

		// get user ratios
		DBCollection orgBehaviourProcessInputData = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData();
		DBObject hashtagRatioMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, "USER");
		DBObject urlRatioMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
				MongoCollectionFieldNames.MONGO_URL_RATIO, "USER");
		DBObject mentionRatioMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
				MongoCollectionFieldNames.MONGO_MENTION_RATIO, "USER");
		DBObject friendFollowerRatioMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
				MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO, "USER");
		DBObject userStatusCountMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
				MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT, "USER");
		DBObject userFavoriteCountMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
				MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT, "USER");
		DBObject userHashtagPostCountMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
				MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT, "USER");
		DBObject userCreationDateMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
				MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE, "USER");

		String averageCreationDateStr = String.valueOf(userCreationDateMeanVariance.get("average"));
		String minCreationDateStr = String.valueOf(userCreationDateMeanVariance.get("min"));
		String maxCreationDateStr = String.valueOf(userCreationDateMeanVariance.get("max"));

		Date averageCreationDate = DateTimeUtils.getUTCDateFromRataDieFormat(averageCreationDateStr);
		Date minCreationDate = DateTimeUtils.getUTCDateFromRataDieFormat(minCreationDateStr);
		Date maxCreationDate = DateTimeUtils.getUTCDateFromRataDieFormat(maxCreationDateStr);

		userCreationDateMeanVariance.put("average",
				DateTimeUtils.getUTCDateTimeStringInGenericFormat(averageCreationDate));
		userCreationDateMeanVariance.put("min", DateTimeUtils.getUTCDateTimeStringInGenericFormat(minCreationDate));
		userCreationDateMeanVariance.put("max", DateTimeUtils.getUTCDateTimeStringInGenericFormat(maxCreationDate));

		jsonArray.put(hashtagRatioMeanVariance.toMap());
		jsonArray.put(urlRatioMeanVariance.toMap());
		jsonArray.put(mentionRatioMeanVariance.toMap());
		jsonArray.put(friendFollowerRatioMeanVariance.toMap());
		jsonArray.put(userStatusCountMeanVariance.toMap());
		jsonArray.put(userFavoriteCountMeanVariance.toMap());
		jsonArray.put(userHashtagPostCountMeanVariance.toMap());
		jsonArray.put(userCreationDateMeanVariance.toMap());

		// get cos similarity ratios
		DBCollection orgBehaviourRequestedSimilarityCalculations = DirenajMongoDriver.getInstance()
				.getOrgBehaviourRequestedSimilarityCalculations();

		DBObject hourlyTweetHashtagRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, "COS_SIM");
		DBObject hourlyTweetMentionRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.MONGO_MENTION_RATIO, "COS_SIM");
		DBObject hourlyTweetUrlRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.MONGO_URL_RATIO, "COS_SIM");
		DBObject hourlyRetweetRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.MONGO_RETWEET_RATIO, "COS_SIM");
		DBObject hourlyTweetUserCountRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT_DISTINCT_USER_RATIO, "COS_SIM");
		// tweet similarity
		DBObject hourlyMostSimilarTweetsRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.MOST_SIMILAR, "COS_SIM");
		DBObject hourlyVerySimilarTweetsRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.VERY_SIMILAR, "COS_SIM");
		DBObject hourlySimilarTweetsRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.SIMILAR, "COS_SIM");
		DBObject hourlySlightlySimilarTweetsRatioMeanVariance = getMeanVariance(
				orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.SLIGHTLY_SIMILAR, "COS_SIM");
		DBObject hourlyNonSimilarTweetsRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.NON_SIMILAR, "COS_SIM");

		jsonArray.put(hourlyTweetHashtagRatioMeanVariance.toMap());
		jsonArray.put(hourlyTweetMentionRatioMeanVariance.toMap());
		jsonArray.put(hourlyTweetUrlRatioMeanVariance.toMap());
		jsonArray.put(hourlyRetweetRatioMeanVariance.toMap());
		jsonArray.put(hourlyTweetUserCountRatioMeanVariance.toMap());
		jsonArray.put(hourlyMostSimilarTweetsRatioMeanVariance.toMap());
		jsonArray.put(hourlyVerySimilarTweetsRatioMeanVariance.toMap());
		jsonArray.put(hourlySimilarTweetsRatioMeanVariance.toMap());
		jsonArray.put(hourlySlightlySimilarTweetsRatioMeanVariance.toMap());
		jsonArray.put(hourlyNonSimilarTweetsRatioMeanVariance.toMap());

		Logger.getLogger(MongoPaginationServlet.class)
				.debug("getMeanVariance4All is finished for requestId : " + requestId);

	}

	/**
	 * 
	 * DBObject meanVarianceResult = getMeanVariance(query, requestId,
	 * MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO);
	 * 
	 * @param query
	 * @param requestId
	 * @param calculationColumn
	 * @return
	 */
	private DBObject getMeanVariance(DBCollection collection, BasicDBObject query, String requestId,
			String calculationColumn, String calculationDomain) {
		Logger.getLogger(MongoPaginationServlet.class).debug("getMeanVariance is executed for requestId : " + requestId
				+ ". Column Name : " + calculationColumn + ". CalculationDomain: " + calculationDomain);

		DBObject calculationQuery = new BasicDBObject("requestId", requestId)
				.append("calculationType", calculationColumn).append("calculationDomain", calculationDomain);
		DBObject meanVarianceResult = DirenajMongoDriver.getInstance().getOrgBehaviourRequestMeanVarianceCalculations()
				.findOne(calculationQuery);
		return meanVarianceResult;
	}

	private void visualizeUserCreationTimesInBarChart(JSONArray jsonArray, BasicDBObject query)
			throws Exception, JSONException {
		// get cursor
		// FIXME 20160818 - Tarihe Göre sırala
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query);
		Map<String, Double> usersByDate = new HashMap<>();
		// get objects from cursor
		int userCount = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userCount++;
			String twitterDateStr = (String) next.get(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE);
			String userCreationDate = DateTimeUtils.getStringOfDate("yyyyMM",
					DateTimeUtils.getTwitterDate(twitterDateStr));
			CollectionUtil.incrementKeyValueInMap(usersByDate, userCreationDate);
		}
		CollectionUtil.calculatePercentage(usersByDate, userCount);
		usersByDate = CollectionUtil.sortByComparator4DateKey(usersByDate);
		for (Entry<String, Double> entry : usersByDate.entrySet()) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("creationDate", entry.getKey());
			jsonObject.put("percentage", entry.getValue());
			jsonArray.put(jsonObject);
		}
	}

	private void visualizeHourlyTweetSimilarities(JSONArray jsonArray, BasicDBObject query4CosSimilarityRequest)
			throws Exception, JSONException {
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations()
				.find(query4CosSimilarityRequest)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_COS_SIM_REQ_RATA_DIE_LOWER_TIME, 1));
		// get objects from cursor
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			// prepare json object
			String twitterDateStr = (String) next.get("lowerTimeInterval");
			String twitterDate = DateTimeUtils.getStringOfDate("yyyyMMdd HH:mm",
					DateTimeUtils.getUTCDateTime(DateTimeUtils.getTwitterDate(twitterDateStr)));

			jsonArray.put(new JSONObject().put("time", twitterDate)
					.put(MongoCollectionFieldNames.NON_SIMILAR + " (90 Degree)",
							NumberUtils.roundDouble(4, (double) next.get(MongoCollectionFieldNames.NON_SIMILAR) * 100d,
									100d))
					.put(MongoCollectionFieldNames.SLIGHTLY_SIMILAR + " (60-89 Degree)",
							NumberUtils.roundDouble(4,
									(double) next.get(MongoCollectionFieldNames.SLIGHTLY_SIMILAR) * 100d, 100d))
					.put(MongoCollectionFieldNames.SIMILAR + " (60 Degree)",
							NumberUtils.roundDouble(4, (double) next.get(MongoCollectionFieldNames.SIMILAR) * 100d,
									100d))
					.put(MongoCollectionFieldNames.VERY_SIMILAR + " (30-45 Degree)",
							NumberUtils.roundDouble(4, (double) next.get(MongoCollectionFieldNames.VERY_SIMILAR) * 100d,
									100d))
					.put(MongoCollectionFieldNames.MOST_SIMILAR + " (0-30 Degree)", NumberUtils.roundDouble(4,
							(double) next.get(MongoCollectionFieldNames.MOST_SIMILAR) * 100d, 100d)));
		}
	}

	private void visualizeHourlyUserAndTweetCount(JSONArray jsonArray, BasicDBObject query4CosSimilarityRequest)
			throws Exception, JSONException {
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations()
				.find(query4CosSimilarityRequest)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_COS_SIM_REQ_RATA_DIE_LOWER_TIME, 1));
		JSONArray tweetCountsJsonArray = new JSONArray();
		JSONArray distinctUserCountsJsonArray = new JSONArray();
		// get objects from cursor
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			// prepare json object
			String twitterDateStr = (String) next.get("lowerTimeInterval");
			String twitterDate = DateTimeUtils.getStringOfDate("yyyyMMdd HH:mm",
					DateTimeUtils.getUTCDateTime(DateTimeUtils.getTwitterDate(twitterDateStr)));
			tweetCountsJsonArray.put(new JSONObject().put("time", twitterDate).put("value",
					next.get(MongoCollectionFieldNames.MONGO_DISTINCT_USER_COUNT)));
			// prepare json object
			distinctUserCountsJsonArray.put(new JSONObject().put("time", twitterDate).put("value",
					next.get(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT)));

		}
		// init to array
		jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_DISTINCT_USER_COUNT)
				.put("values", tweetCountsJsonArray));
		jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT).put("values",
				distinctUserCountsJsonArray));
	}

	private void visualizeHourlyEntityRatios(JSONArray jsonArray, BasicDBObject query4CosSimilarityRequest)
			throws Exception, JSONException {
		JSONArray urlRatioJsonArray = new JSONArray();
		JSONArray hashtagRatioJsonArray = new JSONArray();
		JSONArray mentionRatioJsonArray = new JSONArray();
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations()
				.find(query4CosSimilarityRequest)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_COS_SIM_REQ_RATA_DIE_LOWER_TIME, 1));
		// get objects from cursor
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			// prepare json object
			String twitterDateStr = (String) next.get("lowerTimeInterval");
			double urlRatio = (double) next.get(MongoCollectionFieldNames.MONGO_URL_RATIO);
			double hashtagRatio = (double) next.get(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO);
			double mentionRatio = (double) next.get(MongoCollectionFieldNames.MONGO_MENTION_RATIO);

			String twitterDate = DateTimeUtils.getStringOfDate("yyyyMMdd HH:mm",
					DateTimeUtils.getUTCDateTime(DateTimeUtils.getTwitterDate(twitterDateStr)));

			urlRatioJsonArray.put(new JSONObject().put("time", twitterDate).put("value", urlRatio));
			hashtagRatioJsonArray.put(new JSONObject().put("time", twitterDate).put("value", hashtagRatio));
			mentionRatioJsonArray.put(new JSONObject().put("time", twitterDate).put("value", mentionRatio));
		}
		// init to array
		jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_URL_RATIO).put("values",
				urlRatioJsonArray));
		jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_HASHTAG_RATIO).put("values",
				hashtagRatioJsonArray));
		jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_MENTION_RATIO).put("values",
				mentionRatioJsonArray));

	}

	private void visualizeHourlyTweetRatios(JSONArray jsonArray, BasicDBObject query4CosSimilarityRequest)
			throws Exception, JSONException {
		JSONArray retweetRatioJsonArray = new JSONArray();
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations()
				.find(query4CosSimilarityRequest)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_COS_SIM_REQ_RATA_DIE_LOWER_TIME, 1));
		// get objects from cursor
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			// prepare json object
			String twitterDateStr = (String) next.get("lowerTimeInterval");
			double retweetRatio = NumberUtils.roundDouble(4,
					(double) next.get(MongoCollectionFieldNames.MONGO_RETWEET_RATIO), 1) * 100d;

			String twitterDate = DateTimeUtils.getStringOfDate("yyyyMMdd HH:mm",
					DateTimeUtils.getUTCDateTime(DateTimeUtils.getTwitterDate(twitterDateStr)));

			retweetRatioJsonArray.put(new JSONObject().put("time", twitterDate).put("value", retweetRatio));
		}
		// init to array
		jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_RETWEET_RATIO).put("values",
				retweetRatioJsonArray));

	}

	private void visualizeUserRoughTweetCountsInBarChart(JSONArray jsonArray, BasicDBObject query)
			throws JSONException {

		Map<String, Map<String, Double>> ratioValues = new HashMap<>();
		// define limits
		List<String> limits = new ArrayList<>();
		// limits between 0 - 1000
		limits.add("0");
		int previous = 1;
		for (int i = 100; i <= 1000; i = i + 100) {
			limits.add(previous + "-" + i);
			previous = i + 1;
		}
		// limits between 1000 - 10000
		previous = 1001;
		for (int i = 2000; i <= 10000; i = i + 1000) {
			limits.add(previous + "-" + i);
			previous = i + 1;
		}
		limits.add("10000-50000");
		limits.add("50001-100000");
		// limits between 100.000 - 1.000.000
		previous = 100001;
		for (int i = 200000; i <= 1000000; i = i + 100000) {
			limits.add(previous + "-" + i);
			previous = i + 1;
		}
		limits.add("1000001-...");

		// init hash map
		for (String limit : limits) {
			// range percentages
			Map<String, Double> rangePercentages = new HashMap<>();
			rangePercentages.put(MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT, 0d);
			rangePercentages.put(MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT, 0d);
			// add to ratio values
			ratioValues.put(limit, rangePercentages);
		}

		// get cursor
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query);
		// get objects from cursor
		int userCount = 0;
		while (paginatedResult.hasNext()) {
			userCount++;
			DBObject next = paginatedResult.next();
			double favoriteStatusCount = (double) next.get(MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT);
			double statusCount = (double) next.get(MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT);
			CollectionUtil.findGenericRange(limits, ratioValues, MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT,
					favoriteStatusCount);
			CollectionUtil.findGenericRange(limits, ratioValues, MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT,
					statusCount);
		}

		CollectionUtil.calculatePercentageForNestedMap(ratioValues, userCount);
		for (String limit : limits) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ratio", limit);
			jsonObject.put(MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT,
					ratioValues.get(limit).get(MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT));
			jsonObject.put(MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT,
					ratioValues.get(limit).get(MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT));
			jsonArray.put(jsonObject);
		}

	}

	private void visualizeUserPostDeviceRatios(JSONArray jsonArray, BasicDBObject query) throws JSONException {
		// get cursor
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_POST_TWITTER_DEVICE_RATIO, 1));
		JSONArray twitterPostDeviceRatioJsonArray = new JSONArray();
		// get objects from cursor
		int userNo = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userNo++;
			// prepare json object
			twitterPostDeviceRatioJsonArray
					.put(new JSONObject()
							.put("ratioValue",
									NumberUtils.roundDouble(4,
											(double) next
													.get(MongoCollectionFieldNames.MONGO_USER_POST_TWITTER_DEVICE_RATIO)))
					.put("userSequenceNo", userNo));

		}
		paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_POST_MOBILE_DEVICE_RATIO, 1));
		JSONArray mobilePostDeviceRatioJsonArray = new JSONArray();
		// get objects from cursor
		userNo = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userNo++;
			// prepare json object
			mobilePostDeviceRatioJsonArray
					.put(new JSONObject()
							.put("ratioValue",
									NumberUtils.roundDouble(4,
											(double) next
													.get(MongoCollectionFieldNames.MONGO_USER_POST_MOBILE_DEVICE_RATIO)))
					.put("userSequenceNo", userNo));

		}
		paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_THIRD_PARTY_DEVICE_RATIO, 1));
		JSONArray thirdPartyPostRatioJsonArray = new JSONArray();
		// get objects from cursor
		userNo = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userNo++;
			// prepare json object
			thirdPartyPostRatioJsonArray
					.put(new JSONObject()
							.put("ratioValue",
									NumberUtils.roundDouble(4,
											(double) next
													.get(MongoCollectionFieldNames.MONGO_USER_THIRD_PARTY_DEVICE_RATIO)))
					.put("userSequenceNo", userNo));

		}

		// init to array
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_POST_TWITTER_DEVICE_RATIO)
				.put("values", twitterPostDeviceRatioJsonArray));
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_POST_MOBILE_DEVICE_RATIO)
				.put("values", mobilePostDeviceRatioJsonArray));
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_THIRD_PARTY_DEVICE_RATIO)
				.put("values", thirdPartyPostRatioJsonArray));
	}

	private void visualizeUserRoughHashtagTweetCounts(JSONArray jsonArray, BasicDBObject query) throws JSONException {
		// get cursor
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT, 1));
		// get objects from cursor
		int userNo = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userNo++;
			// prepare json object
			JSONObject userProcessInputData = new JSONObject();
			userProcessInputData.put("userSequenceNo", userNo);
			userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_ID,
					TextUtils.getLongValue((String) next.get(MongoCollectionFieldNames.MONGO_USER_ID)));
			// get ratios
			userProcessInputData.put("ratioValue", NumberUtils.roundDouble(4,
					(double) next.get(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT)));
			// init to array
			jsonArray.put(userProcessInputData);
		}
	}

	private void visualizeUserFriendFollowerRatio(JSONArray jsonArray, BasicDBObject query) throws JSONException {
		// get cursor
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO, 1));
		// get objects from cursor
		int userNo = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userNo++;
			// prepare json object
			JSONObject userProcessInputData = new JSONObject();
			userProcessInputData.put("userSequenceNo", userNo);
			userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_ID,
					TextUtils.getLongValue((String) next.get(MongoCollectionFieldNames.MONGO_USER_ID)));
			// get ratios
			userProcessInputData.put("ratioValue", NumberUtils.roundDouble(4,
					(double) next.get(MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO)));
			// init to array
			jsonArray.put(userProcessInputData);
		}
	}

	/**
	 * 
	 * @deprecated
	 * 
	 * @param jsonArray
	 * @param query
	 * @throws JSONException
	 */
	private void visualizeUserTweetEntityRatios(JSONArray jsonArray, BasicDBObject query) throws JSONException {
		// get cursor
		DBCursor urlRatioResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_URL_RATIO, 1));
		JSONArray urlRatioJsonArray = new JSONArray();
		// get objects from cursor
		// get url ratio
		int userNo = 0;
		while (urlRatioResult.hasNext()) {
			DBObject next = urlRatioResult.next();
			userNo++;
			urlRatioJsonArray
					.put(new JSONObject()
							.put("ratioValue",
									NumberUtils.roundDouble(4,
											(double) next.get(MongoCollectionFieldNames.MONGO_URL_RATIO)))
					.put("userSequenceNo", userNo));
		}
		// hashtag ratio
		DBCursor hashtagRatioResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, 1));
		JSONArray hashtagRatioJsonArray = new JSONArray();
		// get objects from cursor
		userNo = 0;
		while (hashtagRatioResult.hasNext()) {
			DBObject next = hashtagRatioResult.next();
			userNo++;
			hashtagRatioJsonArray.put(new JSONObject()
					.put("ratioValue",
							NumberUtils.roundDouble(4,
									(double) next.get(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO)))
					.put("userSequenceNo", userNo));
		}
		// mention ratio
		DBCursor mentionRatioResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_MENTION_RATIO, 1));
		JSONArray mentionRatioJsonArray = new JSONArray();
		// get objects from cursor
		userNo = 0;
		while (mentionRatioResult.hasNext()) {
			DBObject next = mentionRatioResult.next();
			userNo++;
			mentionRatioJsonArray.put(new JSONObject()
					.put("ratioValue",
							NumberUtils.roundDouble(4,
									(double) next.get(MongoCollectionFieldNames.MONGO_MENTION_RATIO)))
					.put("userSequenceNo", userNo));
		}
		// init to array
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_URL_RATIO).put("values",
				urlRatioJsonArray));
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_HASHTAG_RATIO).put("values",
				hashtagRatioJsonArray));
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_MENTION_RATIO).put("values",
				mentionRatioJsonArray));
	}

	private void visualizeUserCreationTimes(JSONArray jsonArray, BasicDBObject query) throws JSONException, Exception {
		// get cursor
		// FIXME 20160818 - Tarihe Göre sırala
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE, 1));
		// get objects from cursor
		int userNo = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userNo++;
			String twitterDateStr = (String) next.get(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE);

			JSONObject userProcessInputData = new JSONObject();
			userProcessInputData.put("userSequenceNo", userNo);
			userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_ID,
					TextUtils.getLongValue((String) next.get(MongoCollectionFieldNames.MONGO_USER_ID)));
			userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE,
					DateTimeUtils.getStringOfDate("yyyyMMdd", DateTimeUtils.getTwitterDate(twitterDateStr)));
			jsonArray.put(userProcessInputData);
		}
	}

}
