package aishields;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import aishields.experiment.ExperimentAggregator;
import aishields.experiment.ExperimentResult;
import aishields.experiment.Row;
import aishields.utils.StatisticsCounter;
import aishields.utils.Utils;

public class ProcessSurveyData {
	
	private static final String EYE_V = "eye";
	private static final String SHIELD_V = "shield";
	private static final String GENDER_V = "gender";
	private static final String GEO_V = "geolocation";
	private static final String LINKPRED_V = "linkpred";
	private static final String HUMAN_V = "Human";
	private static final String AI_V = "AI";
	private static final List<String> TASKS = Utils.aList(GENDER_V, GEO_V, LINKPRED_V);
	
	private static final String ROOT_DIR = "output/ai-shields/survey-data/";
	
	private static final String INPUT_DIR = ROOT_DIR + "input/";
	private static final String GENDER_EYE_FILE = INPUT_DIR + "gender-eye.csv";
	private static final String GENDER_SHIELD_FILE = INPUT_DIR + "gender-shield.csv";
	private static final String GEOLOCATION_EYE_FILE = INPUT_DIR + "geolocation-eye.csv";
	private static final String GEOLOCATION_SHIELD_FILE = INPUT_DIR + "geolocation-shield.csv";
	private static final String LINKPRED_EYE_FILE = INPUT_DIR + "linkpred-eye.csv";
	private static final String LINKPRED_SHIELD_FILE = INPUT_DIR + "linkpred-shield.csv";
	
	private static final String DATA_DIR = ROOT_DIR + "data/";
	private static final String EYE_DATA_FILE = DATA_DIR + "eye.csv";
	private static final String SHIELD_DATA_FILE = DATA_DIR + "shield.csv";
	
	// Columns with demographics in the data
	private static final String AGE_COL = "age";
	private static final String GENDER_COL = "gender";
	private static final String ETHNICITY_COL = "ethnicity";
	private static final String ATTITUDE_COL = "attitude";
	private static final String EDUCATION_COL = "education";
	private static final String INCOME_COL = "income";
	private static final List<String> DEMOGRAPHICS_COLS =
			Utils.aList(AGE_COL, GENDER_COL, ETHNICITY_COL, ATTITUDE_COL, EDUCATION_COL, INCOME_COL);
	
	public static void main(String[] args) {
		generatePowerAnalysisInput();
		generateTTestsInput();
		processBasicResults();
		processCorrectIncorrectEyeResults();
		processHighLowShieldResults();
	}
	
	private static Map<String, Map<Integer, String>> loadBestShieldAnswers(){
		Map<String, Map<Integer, String>> res = new HashMap<>();
		res.put(GENDER_V, ExperimentResult.loadRows(GENDER_SHIELD_FILE).collect(Collectors.toMap(
				row -> row.getInt("id"),
				row -> normalizeKey(row.get("best").split(":")[0], false))));
		res.put(GEO_V, ExperimentResult.loadRows(GEOLOCATION_SHIELD_FILE).collect(Collectors.toMap(
				row -> row.getInt("id"),
				row -> normalizeKey(row.get("best").split(":")[0], false))));
		res.put(LINKPRED_V, ExperimentResult.loadRows(LINKPRED_SHIELD_FILE).collect(Collectors.toMap(
				row -> row.getInt("id"),
				row -> normalizeKey(row.get("best").split(":")[0], false))));
		return res;
	}
	
	private static Map<String, Map<Integer, List<Integer>>> loadHighShieldAnswers(){
		Map<String, Map<Integer, List<Integer>>> res = new HashMap<>();
		res.put(GENDER_V, ExperimentResult.loadRows(GENDER_SHIELD_FILE).collect(Collectors.toMap(
				row -> row.getInt("id"), row -> Utils.aList(0, 1, 2, 3))));
		res.put(GEO_V, ExperimentResult.loadRows(GEOLOCATION_SHIELD_FILE).collect(Collectors.toMap(
				row -> row.getInt("id"),
				row -> {
					List<Integer> all = Stream.of((row.get("high")+","+row.get("low")).split(","))
							.map(s -> Integer.parseInt(s)).sorted().toList();
					return Stream.of(row.get("high").split(",")).map(s -> Integer.parseInt(s)).map(i -> all.indexOf(i)).toList();
				})));
		res.put(LINKPRED_V, ExperimentResult.loadRows(LINKPRED_SHIELD_FILE).collect(Collectors.toMap(
				row -> row.getInt("id"), row -> Utils.aList(0, 1, 4, 5)))); // [add, add, rem, rem]
		return res;
	}
	
	private static boolean genderAICorrect(Row r) {
		return r.getDouble("algProb") > .5;
	}
	
	private static boolean geoAICorrect(Row r) {
		return r.getBool("algCorrect");
	}
	
	private static boolean linkpredAICorrect(Row r) {
		return r.getDouble("algProb") == 1.;
	}
	
	private static String normalizeKey(String key, boolean minusOne) {
		if (key.chars().allMatch(c -> Character.isDigit(c)))
			return key.chars().map(x -> Character.getNumericValue(x)).map(x -> minusOne ? x - 1 : x).sorted()
					.mapToObj(x -> Integer.toString(x)).collect(Collectors.joining());
		else
			return key.chars().map(x -> Character.getNumericValue(x) - 10).sorted()
					.mapToObj(x -> Integer.toString(x)).collect(Collectors.joining());			
	}
	
	private static void generatePowerAnalysisInput() {
		System.out.println("Generating power analysis input...");
		ExperimentResult res = new ExperimentResult(ROOT_DIR + "aishields-power-analysis.csv", Utils.aList("power-analysis"),
				Utils.aList("task", "humanMean", "aiMean", "stdDev"));

		ExperimentResult humanEye = ExperimentResult.loadResult(EYE_DATA_FILE);
		Map<String, Double> humanEyeMeans = ExperimentAggregator.aggregate(humanEye.stream(), Utils.aList("task"), "score")
				.collect(Collectors.toMap(r -> r.get("task"), r -> r.getDouble("scoreMean")));
		Map<String, StatisticsCounter> eyeStats = getAiEyeStatistics();
		Map<String, Double> aiEyeMeans = TASKS.stream().collect(Collectors.toMap(t -> t, t -> eyeStats.get(t).getMean()));
		humanEye.forEach(r -> eyeStats.get(r.get("task")).addValue(r.getDouble("score")));
		TASKS.forEach(t -> res.addRowDontPrint(EYE_V + "-" + t,
				humanEyeMeans.get(t), aiEyeMeans.get(t), eyeStats.get(t).getSD()));
		
		ExperimentResult humanShield = ExperimentResult.loadResult(SHIELD_DATA_FILE);
		Map<String, Double> humanShieldMeans = ExperimentAggregator.aggregate(humanShield.stream(), Utils.aList("task"), "score")
				.collect(Collectors.toMap(r -> r.get("task"), r -> r.getDouble("scoreMean")));
		Map<String, StatisticsCounter> shieldStats = getAiShieldStatistics();
		Map<String, Double> aiShieldMeans = TASKS.stream().collect(Collectors.toMap(t -> t, t -> shieldStats.get(t).getMean()));
		humanShield.forEach(r -> shieldStats.get(r.get("task")).addValue(r.getDouble("score")));
		TASKS.forEach(t -> res.addRowDontPrint(SHIELD_V + "-" + t,
				humanShieldMeans.get(t), aiShieldMeans.get(t), shieldStats.get(t).getSD()));
		
		res.saveResult();
		System.out.println("Finished generating power analysis input.");
	}
	
	private static void generateTTestsInput() {
		System.out.println("Generating T-tests input...");
		ExperimentResult res = new ExperimentResult(ROOT_DIR + "aishields-ttests.csv", Utils.aList("ttests"),
				Utils.concat(Utils.aList("task", "performer", "score"), DEMOGRAPHICS_COLS));

		Map<String, Map<Integer, Integer>> aiEye = getAiEyeScores();
		ExperimentResult humanEye = ExperimentResult.loadResult(EYE_DATA_FILE);
		for (Row r : humanEye) {
			res.addRowDontPrint(Utils.concat(
					new Object[] {EYE_V + "-" + r.get("task"), HUMAN_V, r.get("score")},
					DEMOGRAPHICS_COLS.stream().map(col -> scatterBin(col, r)).toArray()));
			res.addRowDontPrint(Utils.concat(
					new Object[] {EYE_V + "-" + r.get("task"), AI_V, aiEye.get(r.get("task")).get(r.getInt("questionId"))},
					DEMOGRAPHICS_COLS.stream().map(col -> "").toArray()));
		}
		
		Map<String, Map<Integer, Double>> aiShield = getAiShieldScores();
		ExperimentResult humanShield = ExperimentResult.loadResult(SHIELD_DATA_FILE);
		for (Row r : humanShield) {
			res.addRowDontPrint(Utils.concat(
					new Object[] {SHIELD_V + "-" + r.get("task"), HUMAN_V, r.get("score")},
					DEMOGRAPHICS_COLS.stream().map(col -> scatterBin(col, r)).toArray()));
			res.addRowDontPrint(Utils.concat(
					new Object[] {SHIELD_V + "-" + r.get("task"), AI_V, aiShield.get(r.get("task")).get(r.getInt("questionId"))},
					DEMOGRAPHICS_COLS.stream().map(col -> "").toArray()));
		}
		
		res.saveResult();
		System.out.println("Finished generating T-tests input.");
	}
	
	private static void processBasicResults() {
		ExperimentResult res = new ExperimentResult(ROOT_DIR + "basic-bars/", Utils.aList("basic-bars"),
				Utils.aList("taskType", "task", "performer", "scoreMean", "scoreC95"));
		getAiEyeStatistics().forEach((t,sc) -> res.addRowDontPrint(EYE_V, t, AI_V, sc.getMean(), sc.getConf95()));		
		ExperimentAggregator.aggregate(ExperimentResult.loadRows(EYE_DATA_FILE), Utils.aList("task"), "score")
				.forEach(r -> res.addRowDontPrint(EYE_V, r.get("task"), HUMAN_V, r.get("scoreMean"), r.get("scoreC95")));
		
		getAiShieldStatistics().forEach((t,sc) -> res.addRowDontPrint(SHIELD_V, t, AI_V, sc.getMean(), sc.getConf95()));
		ExperimentAggregator.aggregate(ExperimentResult.loadRows(SHIELD_DATA_FILE), Utils.aList("task"), "score")
				.forEach(r -> res.addRowDontPrint(SHIELD_V, r.get("task"), HUMAN_V, r.get("scoreMean"), r.get("scoreC95")));
		
		Map<String, Map<String, Double>> baselines = Map.of(
				EYE_V, Stream.of(ExperimentResult.loadHeader(EYE_DATA_FILE).get(1).split(",")).map(s -> s.split(":"))
						.collect(Collectors.toMap(ps -> ps[0], ps -> Double.parseDouble(ps[1]))),
				SHIELD_V, Stream.of(ExperimentResult.loadHeader(SHIELD_DATA_FILE).get(1).split(",")).map(s -> s.split(":"))
						.collect(Collectors.toMap(ps -> ps[0], ps -> Double.parseDouble(ps[1]))));
		res.addDoubleColumn("baseline", r -> baselines.get(r.get("taskType")).get(r.get("task")));
		res.addColumn("label", r -> r.concatVals("taskType", "task"));
		res.addColumn("taskRead", r -> r.get("task").equals(LINKPRED_V) ? "Link prediction" : Utils.capitalize(r.get("task")));
		res.saveResult();
	}
	
	private static Map<String, Map<Integer, Integer>> getAiEyeScores(){
		Map<String, Map<Integer, Integer>> scores = new HashMap<>();
		scores.put(GENDER_V, ExperimentResult.loadResult(GENDER_EYE_FILE).stream()
				.collect(Collectors.toMap(r -> r.getInt("id"), r -> genderAICorrect(r) ? 1 : 0)));
		scores.put(GEO_V, ExperimentResult.loadResult(GEOLOCATION_EYE_FILE).stream()
				.collect(Collectors.toMap(r -> r.getInt("id"), r -> geoAICorrect(r) ? 1 : 0)));
		scores.put(LINKPRED_V, ExperimentResult.loadResult(LINKPRED_EYE_FILE).stream()
				.collect(Collectors.toMap(r -> r.getInt("id"), r -> linkpredAICorrect(r) ? 1 : 0)));
		return scores;
	}

	private static Map<String, Map<Integer, Double>> getAiShieldScores(){
		Map<String, Map<Integer, Double>> scores = new HashMap<>();
		scores.put(GENDER_V, ExperimentResult.loadResult(GENDER_SHIELD_FILE).stream()
				.collect(Collectors.toMap(r -> r.getInt("id"), r -> Double.parseDouble(r.get("best").split(":")[1]))));
		scores.put(GEO_V, ExperimentResult.loadResult(GEOLOCATION_SHIELD_FILE).stream()
				.collect(Collectors.toMap(r -> r.getInt("id"), r -> Double.parseDouble(r.get("best").split(":")[1]))));
		scores.put(LINKPRED_V, ExperimentResult.loadResult(LINKPRED_SHIELD_FILE).stream()
				.collect(Collectors.toMap(r -> r.getInt("id"), r -> Double.parseDouble(r.get("best").split(":")[1]))));
		return scores;
	}
	
	private static Map<String, StatisticsCounter> getAiEyeStatistics(){
		Map<String, Map<Integer, Integer>> scores = getAiEyeScores();
		Map<String, StatisticsCounter> res = TASKS.stream().collect(Collectors.toMap(t -> t, t -> new StatisticsCounter()));
		ExperimentResult.loadResult(EYE_DATA_FILE).stream()
				.forEach(r -> res.get(r.get("task")).addValue(scores.get(r.get("task")).get(r.getInt("questionId"))));
		return res;
	}
	
	private static Map<String, StatisticsCounter> getAiShieldStatistics(){
		Map<String, Map<Integer, Double>> scores = getAiShieldScores();
		Map<String, StatisticsCounter> res = TASKS.stream().collect(Collectors.toMap(t -> t, t -> new StatisticsCounter()));
		ExperimentResult.loadResult(SHIELD_DATA_FILE).stream()
				.forEach(r -> res.get(r.get("task")).addValue(scores.get(r.get("task")).get(r.getInt("questionId"))));
		return res;
	}

	// Demographics scatter bin names
	private static final String SCATTER_BIN_BELOW30 = "30 or below";
	private static final String SCATTER_BIN_30TO55 = "Between 30 and 55";
	private static final String SCATTER_BIN_ABOVE55 = "55 or above";
	private static final String SCATTER_BIN_HIGHSCHOOL = "High school";
	private static final String SCATTER_BIN_COLLEGE = "Some college";
	private static final String SCATTER_BIN_DEGREE = "Bachelor or more";
	private static final String SCATTER_BIN_BELOW30K = "Below $30k";
	private static final String SCATTER_BIN_30KTO80K = "$30k to $80k";
	private static final String SCATTER_BIN_ABOVE80K = "Above $80k";
	
	private static String scatterBin(String col, Row r) {
		String v = r.get(col);
		switch (col) {
		case AGE_COL:
			return Integer.parseInt(v) <= 30 ? SCATTER_BIN_BELOW30 :
					Integer.parseInt(v) < 55 ? SCATTER_BIN_30TO55 : SCATTER_BIN_ABOVE55;
		case ATTITUDE_COL:
			return v.replace("Extremely ", "").replace("Slightly ", "");
		case EDUCATION_COL:
			return v.contains("Graduate") || v.contains("Bachelor") ? SCATTER_BIN_DEGREE
					: v.contains("Associate") || v.contains("college") ? SCATTER_BIN_COLLEGE : SCATTER_BIN_HIGHSCHOOL;
		case INCOME_COL:
			return Integer.parseInt(v.substring(v.indexOf("$") + 1, v.indexOf(","))) < 20 ? SCATTER_BIN_BELOW30K
					: Integer.parseInt(v.substring(v.indexOf("$") + 1, v.indexOf(","))) < 70 ? SCATTER_BIN_30KTO80K
							: SCATTER_BIN_ABOVE80K;
		default:
			return v;
		}
	}
	
	private static void processCorrectIncorrectEyeResults() {
		Map<List<String>, List<Row>> humanAnswers = ExperimentResult.loadResult(EYE_DATA_FILE).groupByKey("task", "questionId");
		ExperimentResult raw = new ExperimentResult(new File(ROOT_DIR + "correct-incorrect/"), Utils.aList("correct-incorrect"),
				ExperimentAggregator.aggregate(
						ExperimentResult.loadRows(EYE_DATA_FILE), Utils.aList("task", "questionId"), "score"));
		// Humans are correct if the correct answer is the most common answer among participants
		raw.addBoolColumn("humanCorrect", r -> {
			List<Row> rs = humanAnswers.get(r.getKey("task", "questionId"));
			if (rs.stream().allMatch(rr -> rr.getDouble("score") == 0))
				return false;
			Map<String, Integer> count = Utils.count(rs.stream().map(rr -> rr.get("answer")));
			return Utils.argmax(count.keySet(), a -> count.get(a)).equals(
					rs.stream().filter(rr -> rr.getDouble("score") > 0).findAny().get().get("answer"));
		});
		Map<String, Map<Integer, Boolean>> aiCorrect = new HashMap<>();
		aiCorrect.put(GENDER_V, ExperimentResult.loadRows(GENDER_EYE_FILE).collect(Collectors.toMap(
				r -> r.getInt("id"), r -> genderAICorrect(r))));
		aiCorrect.put(GEO_V, ExperimentResult.loadRows(GEOLOCATION_EYE_FILE).collect(Collectors.toMap(
				r -> r.getInt("id"), r -> geoAICorrect(r))));
		aiCorrect.put(LINKPRED_V, ExperimentResult.loadRows(LINKPRED_EYE_FILE).collect(Collectors.toMap(
				r -> r.getInt("id"), r -> linkpredAICorrect(r))));
		raw.addBoolColumn("aiCorrect", r -> aiCorrect.get(r.get("task")).get(r.getInt("questionId")));

		ExperimentResult res = new ExperimentResult(raw.getResultDir(), raw.getHeader(),
				ExperimentAggregator.histogram(raw.stream(), Utils.aList("task", "humanCorrect", "aiCorrect")));
		res.addPercColumn("perc", "count", Utils.aList("task"));
		for (List<Row> rs : res.groupByKey("task").values()) {
			String task = rs.get(0).get("task");
			if (rs.size() < 4)
				for (Boolean h : new boolean[]{true, false})
					for (Boolean ai : new boolean[]{true, false})
						if (!rs.stream().anyMatch(r -> r.getBool("humanCorrect").equals(h) && r.getBool("aiCorrect").equals(ai)))
							res.addRowDontPrint(task, h, ai, 0, 0.);
		}
		res.addColumn("percRead", r -> String.format("%.2f", r.getDouble("perc") * 100.) + "%");
		res.addColumn("humanRead", r -> r.getBool("humanCorrect") ? "Correct" : "Incorrect");
		res.addColumn("aiRead", r -> r.getBool("aiCorrect") ? "Correct" : "Incorrect");
		res.saveResult();
	}
	
	private static void processHighLowShieldResults() {
		Map<String, Map<Integer, List<Integer>>> highAnswers = loadHighShieldAnswers();
		ExperimentResult raw = new ExperimentResult(new File(ROOT_DIR + "high-low/"), Utils.aList("high-low"),
				Utils.aList("task", "performer", "high"));
		ExperimentResult.loadRows(SHIELD_DATA_FILE).forEach(r -> raw.addRowDontPrint(
				r.get("task"), HUMAN_V,
				Stream.of(r.get("answer").split("")).mapToInt(s -> Integer.parseInt(s) - 1) // normalization
						.filter(i -> highAnswers.get(r.get("task")).get(r.getInt("questionId")).contains(i)).count()));
		loadBestShieldAnswers().forEach((task, m) -> m.forEach((id, ans) -> raw.addRowDontPrint(
				task, AI_V,
				Stream.of(ans.split("")).mapToInt(s -> Integer.parseInt(s)) // already normalized
						.filter(i -> highAnswers.get(task).get(id).contains(i)).count())));
		
		ExperimentResult res = new ExperimentResult(raw.getResultDir(), raw.getHeader(),
				ExperimentAggregator.histogram(raw.stream(), Utils.aList("task", "performer", "high")));
		res.addPercColumn("perc", "count", Utils.aList("task", "performer"));
		res.addColumn("percRead", r -> String.format("%.2f", r.getDouble("perc") * 100.) + "%");
		String suff = " high-impact answer";
		String suffs = suff + "s";
		res.addColumn("highRead", r -> r.getInt("high") + (r.getInt("high") == 1 ? suff : suffs));
		res.addIntColumn("sortX", r -> r.get("performer").equals(AI_V) ? 1 : 0);
		res.saveResult();
	}
}
