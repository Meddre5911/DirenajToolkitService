/**
 * 
 */
function prepareUserCreationTimeGraph(){
	/* 
	 * value accessor - returns the value to encode for a given data object.
	 * scale - maps value to a visual display encoding, such as a pixel position.
	 * map function - maps from data value to display value
	 * axis - sets up axis
	 */ 
	 
	 d3.json("organizedBehaviorCampaignVisualizer?requestType=visualizeUserCreationTimes&requestId=" + $('#requestId').val(), function(error, data) {

	 	var parseDate = d3.time.format("%Y%m%d").parse;

	    data.forEach(function(d) {
	        d.creationDate = parseDate(d.creationDate);
	    });
	    
	    
	    var oneDay = 24*60*60*1000; // hours*minutes*seconds*milliseconds
	    var dateStart = d3.min(data, function(d) { return d.creationDate; });
	    var dateFinish = d3.max(data, function(d) { return d.creationDate; });
	    var numberDays = Math.round(Math.abs((dateStart.getTime() -
	                               dateFinish.getTime())/(oneDay)));

	    var margin = {top: 20, right: 20, bottom: 20, left: 50},
	        height = 400,
	        width = numberDays * 0.2; 

		// var margin = {top: 20, right: 20, bottom: 30, left: 40},
		//  width = 960 - margin.left - margin.right,
		//  height = 500 - margin.top - margin.bottom;


	 
		// setup x 
		var xValue = function(d) { return d.userSequenceNo;}, // data -> value
		    xScale = d3.scale.linear().range([0, width]), // value -> display
		    xMap = function(d) { return xScale(xValue(d));}, // data -> display
		    xAxis = d3.svg.axis().scale(xScale).orient("bottom");
		
		// setup y
		var yValue = function(d) { return  d.creationDate;}, // data -> value
		    yScale = d3.time.scale().range([height, 0]), // value -> display
		    yMap = function(d) { return yScale(yValue(d));}, // data -> display
		    yAxis = d3.svg.axis().scale(yScale).orient("left");
		
		// setup fill color
		var cValue = function(d) { return "";},
		    color = d3.scale.category10();
		
		// add the graph canvas to the body of the webpage
		var svg = d3.select("#creationTimeGraph").append("svg")
		    .attr("width", width + margin.left + margin.right)
		    .attr("height", height + margin.top + margin.bottom)
		  .append("g")
		    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
		
		// add the tooltip area to the webpage
		var tooltip = d3.select("body").append("div")
		    .attr("class", "tooltip")
		    .style("opacity", 0);
		    
		// don't want dots overlapping axis, so add in buffer to data domain
		xScale.domain([d3.min(data, xValue), d3.max(data, xValue)]);
		yScale.domain(d3.extent(data, yValue));

		  // x-axis
		svg.append("g")
		      .attr("class", "x axis")
		      .attr("transform", "translate(0," + height + ")")
		      .call(xAxis.tickFormat(""))
		    .append("text")
		      .attr("class", "label")
		      .attr("x", width)
		      .attr("y", -6)
		      .style("text-anchor", "end")
		      .text("Users");
		
		// y-axis
		svg.append("g")
		      .attr("class", "y axis")
		      .call(yAxis)
		    .append("text")
		      .attr("class", "label")
		      .attr("transform", "rotate(-90)")
		      .attr("y", 6)
		      .attr("dy", ".71em")
		      .style("text-anchor", "end")
		      .text("Creation Date");
		
		  // draw dots
		  svg.selectAll(".dot")
		      .data(data)
		    .enter().append("circle")
		      .attr("class", "dot")
		      .attr("r", 3.5)
		      .attr("cx", xMap)
		      .attr("cy", yMap)
		      .style("fill", function(d) { return color(cValue(d));}); 

	});
}

function prepareMultiLineUserRatiosGraph(requestType,divId,yLineText){

	d3.json("organizedBehaviorCampaignVisualizer?requestType="+requestType+"&requestId=" + $('#requestId').val(), function(error, data) {
		
		
		var margin = {
			    top: 20,
			    right: 80,
			    bottom: 30,
			    left: 50
			},
			width = 700 - margin.left - margin.right,
			height = 400 - margin.top - margin.bottom;


			var x = d3.scale.linear()
			    .range([0, width]);

			var y = d3.scale.linear()
			    .range([height, 0]);

			var color = d3.scale.category10();

			var xAxis = d3.svg.axis()
			    .scale(x)
			    .orient("bottom");

			var yAxis = d3.svg.axis()
			    .scale(y)
			    .orient("left");

			var line = d3.svg.line()
			    .interpolate("basis")
			    .x(function (d) {
			    return x(d.userSequenceNo);
			})
			    .y(function (d) {
			    return y(d.ratioValue);
			});

			var svg = d3.select("#"+divId).append("svg")
			    .attr("width", width + margin.left + margin.right)
			    .attr("height", height + margin.top + margin.bottom)
			    .append("g")
			    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

			color.domain(data.map(function (d) { return d.ratioType; }));


			var cities = data;

			var minX = d3.min(data, function (kv) { return d3.min(kv.values, function (d) { return d.userSequenceNo; }) });
			var maxX = d3.max(data, function (kv) { return d3.max(kv.values, function (d) { return d.userSequenceNo; }) });
			var minY = d3.min(data, function (kv) { return d3.min(kv.values, function (d) { return d.ratioValue; }) });
			var maxY = d3.max(data, function (kv) { return d3.max(kv.values, function (d) { return d.ratioValue; }) });

			x.domain([minX, maxX+(maxX*0.3)]);
			y.domain([minY, maxY+(maxY*0.3)]);

			svg.append("g")
			    .attr("class", "x axis")
			    .attr("transform", "translate(0," + height + ")")
			    .call(xAxis.tickFormat(""));

			svg.append("g")
			    .attr("class", "y axis")
			    .call(yAxis)
			    .append("text")
			    .attr("transform", "rotate(-90)")
			    .attr("y", 6)
			    .attr("dy", ".71em")
			    .style("text-anchor", "end")
			    .text(yLineText);

			var city = svg.selectAll(".city")
			    .data(cities)
			    .enter().append("g")
			    .attr("class", "city");

			city.append("path")
			    .attr("class", "line")
			    .attr("d", function (d) {
			    return line(d.values);
			})
			    .style("stroke", function (d) {
			    return color(d.ratioType);
			});

			city.append("text")
			    .datum(function (d) {
			    return {
			    	ratioType: d.ratioType,
			    	userSequenceNo: d.values[d.values.length - 1].userSequenceNo,
			    	ratioValue: d.values[d.values.length - 1].ratioValue
			    };
			})
			    .attr("transform", function (d) {
			    return "translate(" + x(d.userSequenceNo) + "," + y(d.ratioValue) + ")";
			})
			    .attr("x", 3)
			    .attr("dy", ".35em")
			    .text(function (d) {
			        return d.ratioType;
			});
		  
			
		var legend = svg.selectAll(".legend")
		      .data(color.domain())
		    .enter().append("g")
		      .attr("class", "legend")
		      .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

		  legend.append("rect")
		      .attr("x", width - 18)
		      .attr("width", 18)
		      .attr("height", 18)
		      .style("fill", color);

		  legend.append("text")
		      .attr("x", width - 24)
		      .attr("y", 9)
		      .attr("dy", ".35em")
		      .style("text-anchor", "end")
		      .text(function(d) { return d; });
		  
		});
}

function prepareUserRatiosGraph(requestType,divId,yLineText){

	d3.json("organizedBehaviorCampaignVisualizer?requestType="+requestType+"&requestId=" + $('#requestId').val(), function(error, data) {
		if (error) throw error;

		var margin = {top: 20, right: 20, bottom: 30, left: 50},
	    width = 700 - margin.left - margin.right,
	    height = 400 - margin.top - margin.bottom;
	
	
		var x = d3.scale.linear()
		    .range([0, width]);
	
		var y = d3.scale.linear()
		    .range([height, 0]);
	
		var xAxis = d3.svg.axis()
		    .scale(x)
		    .orient("bottom");
	
		var yAxis = d3.svg.axis()
		    .scale(y)
		    .orient("left");
	
		var line = d3.svg.line()
		    .x(function(d) { return x(d.userSequenceNo); })
		    .y(function(d) { return y(d.ratioValue); });
	
		var svg = d3.select("#"+divId).append("svg")
		    .attr("width", width + margin.left + margin.right)
		    .attr("height", height + margin.top + margin.bottom)
		  .append("g")
		    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	
		  
	
		  x.domain([d3.min(data, function(d) { return d.userSequenceNo; }), d3.max(data, function(d) { return d.userSequenceNo; })]);
		  y.domain([d3.min(data, function(d) { return d.ratioValue; }), d3.max(data, function(d) { return d.ratioValue; })]);
		  
	
		  svg.append("g")
		      .attr("class", "x axis")
		      .attr("transform", "translate(0," + height + ")")
		      .call(xAxis.tickFormat(""));
	
		  svg.append("g")
		      .attr("class", "y axis")
		      .call(yAxis)
		    .append("text")
		      .attr("transform", "rotate(-90)")
		      .attr("y", 6)
		      .attr("dy", ".71em")
		      .style("text-anchor", "end")
		      .text(yLineText);
	
		  svg.append("path")
		      .datum(data)
		      .attr("class", "line")
		      .attr("d", line);
		  
	});
}


function prepareMultiLineUserRatiosGraphInDate(requestType,divId,yLineText){

	d3.json("organizedBehaviorCampaignVisualizer?requestType="+requestType+"&requestId=" + $('#requestId').val(), function(error, data) {
		
	 	var parseDate = d3.time.format("%Y%m%d %H:%M").parse;

	    data.forEach(function(d) {
	    	d.values.forEach(function(p){
	    		p.time = parseDate(p.time);
	    	})
	    });
		
		var margin = {
			    top: 20,
			    right: 80,
			    bottom: 30,
			    left: 50
			},
			width = 700 - margin.left - margin.right,
			height = 400 - margin.top - margin.bottom;


			var x = d3.time.scale()
			    .range([0, width]);

			var y = d3.scale.linear()
			    .range([height, 0]);

			var color = d3.scale.category10();

			var xAxis = d3.svg.axis()
			    .scale(x)
			    .orient("bottom");

			var yAxis = d3.svg.axis()
			    .scale(y)
			    .orient("left");

			var line = d3.svg.line()
			    .interpolate("basis")
			    .x(function (d) {
			    return x(d.time);
			})
			    .y(function (d) {
			    return y(d.value);
			});

			var svg = d3.select("#"+divId).append("svg")
			    .attr("width", width + margin.left + margin.right)
			    .attr("height", height + margin.top + margin.bottom)
			    .append("g")
			    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

			color.domain(data.map(function (d) { return d.valueType; }));


			var cities = data;

			var minX = d3.min(data, function (kv) { return d3.min(kv.values, function (d) { return d.time; }) });
			var maxX = d3.max(data, function (kv) { return d3.max(kv.values, function (d) { return d.time; }) });
			var minY = d3.min(data, function (kv) { return d3.min(kv.values, function (d) { return d.value; }) });
			var maxY = d3.max(data, function (kv) { return d3.max(kv.values, function (d) { return d.value; }) });

//			x.domain(d3.extent(data,function (kv) { return d3.min(kv.values, function (d) { return d.time; }) }));
			x.domain([minX, maxX]);
			y.domain([minY, maxY+(maxY*0.3)]);

			svg.append("g")
			    .attr("class", "x axis")
			    .attr("transform", "translate(0," + height + ")")
			    .call(xAxis);

			svg.append("g")
			    .attr("class", "y axis")
			    .call(yAxis)
			    .append("text")
			    .attr("transform", "rotate(-90)")
			    .attr("y", 6)
			    .attr("dy", ".71em")
			    .style("text-anchor", "end")
			    .text(yLineText);

			var city = svg.selectAll(".city")
			    .data(cities)
			    .enter().append("g")
			    .attr("class", "city");

			city.append("path")
			    .attr("class", "line")
			    .attr("d", function (d) {
			    return line(d.values);
			})
			    .style("stroke", function (d) {
			    return color(d.valueType);
			});

			city.append("text")
			    .datum(function (d) {
			    return {
			    	valueType: d.valueType,
			    	time: d.values[d.values.length - 1].time,
			    	value: d.values[d.values.length - 1].value
			    };
			})
			    .attr("transform", function (d) {
			    return "translate(" + x(d.time) + "," + y(d.value) + ")";
			})
			    .attr("x", 3)
			    .attr("dy", ".35em")
			    .text(function (d) {
			        return d.valueType;
			});
		  
			
		var legend = svg.selectAll(".legend")
		      .data(color.domain())
		    .enter().append("g")
		      .attr("class", "legend")
		      .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

		  legend.append("rect")
		      .attr("x", width - 18)
		      .attr("width", 18)
		      .attr("height", 18)
		      .style("fill", color);

		  legend.append("text")
		      .attr("x", width - 24)
		      .attr("y", 9)
		      .attr("dy", ".35em")
		      .style("text-anchor", "end")
		      .text(function(d) { return d; });
		  
		});
}


function prepareSingleLineUserRatiosGraphInDate(requestType,divId,yLineText){

	d3.json("organizedBehaviorCampaignVisualizer?requestType="+requestType+"&requestId=" + $('#requestId').val(), function(error, data) {
		if (error) throw error;
		
	 	var parseDate = d3.time.format("%Y%m%d %H:%M").parse;

	    data.forEach(function(d) {
	    	d.time = parseDate(d.time);
	    });
	    
	    prepareHourlyCosSimGraph(data,divId+"_NON_SIMILAR","NON_SIMILAR",yLineText);
	    prepareHourlyCosSimGraph(data,divId+"_SLIGHTLY_SIMILAR","SLIGHTLY_SIMILAR",yLineText);
	    prepareHourlyCosSimGraph(data,divId+"_SIMILAR","SIMILAR",yLineText);
	    prepareHourlyCosSimGraph(data,divId+"_VERY_SIMILAR","VERY_SIMILAR",yLineText);
	    prepareHourlyCosSimGraph(data,divId+"_MOST_SIMILAR","MOST_SIMILAR",yLineText);
	    
	});
		
	    
}

function prepareHourlyCosSimGraph(data,divId,similarityRange,yLineText){
	var margin = {top: 20, right: 20, bottom: 30, left: 50},
    width = 700 - margin.left - margin.right,
    height = 400 - margin.top - margin.bottom;


	var x = d3.time.scale()
	    .range([0, width]);

	var y = d3.scale.linear()
	    .range([height, 0]);

	var xAxis = d3.svg.axis()
	    .scale(x)
	    .orient("bottom");

	var yAxis = d3.svg.axis()
	    .scale(y)
	    .orient("left");

	var line = d3.svg.line()
	    .x(function(d) { return x(d.time); })
	    .y(function(d) { return y(d[similarityRange]); });

	var svg = d3.select("#"+divId).append("svg")
	    .attr("width", width + margin.left + margin.right)
	    .attr("height", height + margin.top + margin.bottom)
	  .append("g")
	    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

	  

	  x.domain([d3.min(data, function(d) { return d.time; }), d3.max(data, function(d) { return d.time; })]);
	  y.domain([d3.min(data, function(d) { return d[similarityRange]; }), d3.max(data, function(d) { return d[similarityRange]; })]);
	  

	  svg.append("g")
	      .attr("class", "x axis")
	      .attr("transform", "translate(0," + height + ")")
	      .call(xAxis);

	  svg.append("g")
	      .attr("class", "y axis")
	      .call(yAxis)
	    .append("text")
	      .attr("transform", "rotate(-90)")
	      .attr("y", 6)
	      .attr("dy", ".71em")
	      .style("text-anchor", "end")
	      .text(yLineText);

	  svg.append("path")
	      .datum(data)
	      .attr("class", "line")
	      .attr("d", line);
	
}


function prepareUserCreationTimeGraph(requestType,divId,xAxisName, yAxisName){
	/* 
	 * value accessor - returns the value to encode for a given data object.
	 * scale - maps value to a visual display encoding, such as a pixel position.
	 * map function - maps from data value to display value
	 * axis - sets up axis
	 */ 
	
	 d3.json("organizedBehaviorCampaignVisualizer?requestType="+requestType+"&requestId=" + $('#requestId').val(), function(error, data) {
		   
	    var margin = {top: 20, right: 20, bottom: 70, left: 40},
		    width = 1000,
		    height = 300 - margin.top - margin.bottom;

		// Parse the date / time
		var	parseDate = d3.time.format("%Y%m").parse;

		var x = d3.scale.ordinal().rangeRoundBands([0, width], .05);
		var y = d3.scale.linear().range([height, 0]);

		var xAxis = d3.svg.axis()
		    .scale(x)
		    .orient("bottom")
		    .tickFormat(d3.time.format("%Y%m"));

		var yAxis = d3.svg.axis()
		    .scale(y)
		    .orient("left")
		    .ticks(10);

		var svg = d3.select("#"+divId).append("svg")
		    .attr("width", width + margin.left + margin.right)
		    .attr("height", height + margin.top + margin.bottom)
		  .append("g")
		    .attr("transform", 
		          "translate(" + margin.left + "," + margin.top + ")");


		    data.forEach(function(d) {
		        d.creationDate = parseDate(d.creationDate);
		        d.percentage = +d.percentage;
		    });
			
		  x.domain(data.map(function(d) { return d.creationDate; }));
		  y.domain([0, d3.max(data, function(d) { return d.percentage; })]);

		  
		  
		  svg.append("g")
		      .attr("class", "x axis")
		      .attr("transform", "translate(0," + height + ")")
		      .call(xAxis)
		    .selectAll("text")
		      .style("text-anchor", "end")
		      .attr("dx", "-.8em")
		      .attr("dy", "-.55em")
		      .attr("transform", "rotate(-90)" );

		  svg.append("g")
		      .attr("class", "y axis")
		      .call(yAxis)
		    .append("text")
		      .attr("transform", "rotate(-90)")
		      .attr("y", 6)
		      .attr("dy", ".71em")
		      .style("text-anchor", "end")
		      .text(yAxisName);

		  svg.selectAll("bar")
		      .data(data)
		    .enter().append("rect")
		      .style("fill", "steelblue")
		      .attr("x", function(d) { return x(d.creationDate); })
		      .attr("width", x.rangeBand()*0.5)
		      .attr("y", function(d) { return y(d.percentage); })
		      .attr("height", function(d) { return height - y(d.percentage); });

		    
		});
	
}



function prepareUserRatiosGraphInBarChart(requestType,divId,xAxisName, yAxisName){
	/* 
	 * value accessor - returns the value to encode for a given data object.
	 * scale - maps value to a visual display encoding, such as a pixel position.
	 * map function - maps from data value to display value
	 * axis - sets up axis
	 */ 
	
	 d3.json("organizedBehaviorCampaignVisualizer?requestType="+requestType+"&requestId=" + $('#requestId').val(), function(error, data) {
		   
	    var margin = {top: 20, right: 20, bottom: 70, left: 40},
		    width = 700 - margin.left - margin.right,
		    height = 500 - margin.top - margin.bottom;

		var x = d3.scale.ordinal().rangeRoundBands([0, width], .05);
		var y = d3.scale.linear().range([height, 0]);

		
		var xAxis = d3.svg.axis()
		    .scale(x)
		    .orient("bottom");

		var yAxis = d3.svg.axis()
		    .scale(y)
		    .orient("left")
		    .ticks(10);

		var svg = d3.select("#"+divId).append("svg")
		    .attr("width", width + margin.left + margin.right)
		    .attr("height", height + margin.top + margin.bottom)
		  .append("g")
		    .attr("transform", 
		          "translate(" + margin.left + "," + margin.top + ")");

		  x.domain(data.map(function(d) { return d.ratio; }));
		  y.domain([0, d3.max(data, function(d) { return d.percentage; })]);

		  
		  
		  svg.append("g")
		      .attr("class", "x axis")
		      .attr("transform", "translate(0," + height + ")")
		      .call(xAxis)
		    .selectAll("text")
		      .style("text-anchor", "end")
		      .attr("dx", "-.8em")
		      .attr("dy", "-.55em")
		      .attr("transform", "rotate(-90)" );

		  svg.append("g")
		      .attr("class", "y axis")
		      .call(yAxis)
		    .append("text")
		      .attr("transform", "rotate(-90)")
		      .attr("y", 6)
		      .attr("dy", ".71em")
		      .style("text-anchor", "end")
		      .text(yAxisName);

		  svg.selectAll("bar")
		      .data(data)
		    .enter().append("rect")
		      .style("fill", "steelblue")
		      .attr("x", function(d) { return x(d.ratio); })
		      .attr("width", x.rangeBand()*0.5)
		      .attr("y", function(d) { return y(d.percentage); })
		      .attr("height", function(d) { return height - y(d.percentage); });

		    
		});
}


function prepareGroupedBarChart(requestType,divId,yLineText){

	d3.json("organizedBehaviorCampaignVisualizer?requestType="+requestType+"&requestId=" + $('#requestId').val(), function(error, data) {
		if (error) throw error;
		
	 	var parseDate = d3.time.format("%Y%m%d %H:%M").parse;
	    var margin = {top: 20, right: 20, bottom: 30, left: 40},
	    width = 960 - margin.left - margin.right,
	    height = 500 - margin.top - margin.bottom;

		var x0 = d3.scale.ordinal()
		    .rangeRoundBands([0, width], .5);
	
		var x1 = d3.scale.ordinal();
	
		var y = d3.scale.linear()
		    .range([height, 0]);
	
//		var color = d3.scale.ordinal()
//		    .range(["#98abc5", "#8a89a6", "#7b6888", "#6b486b", "#a05d56"]);

		var color = d3.scale.ordinal()
		.range(["#000066", "#993333", "#003300", "#ffff00", "#98abc5"]);
	
		var xAxis = d3.svg.axis()
		    .scale(x0)
		    .orient("bottom")
		    .tickFormat(d3.time.format("%Y%m%d %H:%M"));
	
		var yAxis = d3.svg.axis()
		    .scale(y)
		    .orient("left")
		    .tickFormat(d3.format(".2s"));
	
		var svg = d3.select("#"+divId).append("svg")
		    .attr("width", width + margin.left + margin.right)
		    .attr("height", 600)
		  .append("g")
		    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	
	
		  var similarityTypes = d3.keys(data[0]).filter(function (key) { return (key !== "time"); });
	
		  data.forEach(function(d) {
		    d.similarities = similarityTypes.map(function(name) { return {name: name, value: +d[name]}; });
		    d.time = parseDate(d.time);
		  });
	
		  x0.domain(data.map(function(d) { return d.time; }));
		  x1.domain(similarityTypes).rangeRoundBands([0, x0.rangeBand()]);
		  y.domain([0, d3.max(data, function(d) { return d3.max(d.similarities, function(d) { return d.value; }); })]);
	
		  svg.append("g")
		      .attr("class", "x axis")
		      .attr("transform", "translate(0," + height + ")")
		      .call(xAxis)
			  .selectAll("text")
		      .style("text-anchor", "end")
		      .attr("dx", "-.8em")
		      .attr("dy", "-.55em")
		      .attr("transform", "rotate(-90)" );
	
		  svg.append("g")
		      .attr("class", "y axis")
		      .call(yAxis)
		    .append("text")
		      .attr("transform", "rotate(-90)")
		      .attr("y", 6)
		      .attr("dy", ".71em")
		      .style("text-anchor", "end")
		      .text(yLineText);
	
		  var state = svg.selectAll(".state")
		      .data(data)
		    .enter().append("g")
		      .attr("class", "state")
		      .attr("transform", function(d) { return "translate(" + x0(d.time) + ",0)"; });
	
		  state.selectAll("rect")
		      .data(function(d) { return d.similarities; })
		    .enter().append("rect")
		      .attr("width", x1.rangeBand()*3)
		      .attr("x", function(d) { return x1(d.name); })
		      .attr("y", function(d) { return y(d.value); })
		      .attr("height", function(d) { return height - y(d.value); })
		      .style("fill", function(d) { return color(d.name); });
	
		  var legend = svg.selectAll(".legend")
		      .data(similarityTypes.slice().reverse())
		    .enter().append("g")
		      .attr("class", "legend")
		      .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });
	
		  legend.append("rect")
		      .attr("x", width - 18)
		      .attr("width", 36)
		      .attr("height", 18)
		      .style("fill", color);
	
		  legend.append("text")
		      .attr("x", width - 24)
		      .attr("y", 9)
		      .attr("dy", ".35em")
		      .style("text-anchor", "end")
		      .text(function(d) { return d; });
	    
	});
		
	    
}




