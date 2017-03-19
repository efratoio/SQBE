import * as d3 from 'd3'
import React, {
    Component
} from 'react';
import ReactDOM from 'react-dom';
import conf from "./constants"

const PresentGraph = React.createClass({

   getInitialState () {
    return {
      initialized: false
    };
  },
    propTypes: {
        links: React.PropTypes.array,
        nodes: React.PropTypes.array,
        example: React.PropTypes.number,
        demi: React.PropTypes.bool
    },
    switchStateHandler(b) {
        this.prop.onSwitchState(b);
    },
    InitializeGraph(nextProps){

        
        var width, height
        var chartWidth, chartHeight
        var margin
        var svg = d3.select(ReactDOM.findDOMNode(this.refs.graph)).append("svg")
        var chartLayer = svg.append("g").classed("chartLayer", true)
        var radius = 6;
        var choosePhase = false;


        main(this.refs.graph, nextProps.nodes.map((x) => {
            x["r"] = radius;
            x["root"] = (x["id"] ==  nextProps.example);
            // x["chosen"] = false;
            return x;
        }), nextProps.links.map((x) => {
            // x["chosen"] = false;
            return x;
        }));

        function main(el, n, l) {
            var range = 100
            var a1 = [{
                    label: "l" + "a",
                    id: 1,
                    r: 10
                }, {
                    label: "l" + "a",
                    id: 2,
                    r: 10
                }],
                a2 = [{
                    source: 1,
                    target: 2
                }];
            var data = {
                nodes: n,
                links: l,
                // nodes:d3.range(0, range).map(function(d){ return {label: "l"+d ,r:~~d3.randomUniform(8, 28)()}}),
                // links:d3.range(0, range).map(function(){ return {source:~~d3.randomUniform(range)(), target:~~d3.randomUniform(range)()} })        
            }
            var size= setSize(el);
            drawChart(data,size[0],size[1]);
        }

        function setSize( el) {
            width = 400;//ReactDOM.findDOMNode(el).getBoundingClientRect().width;
            height = 300;//ReactDOM.findDOMNode(el).getBoundingClientRect().height;
            margin = {
                top: 10,
                left: 10,
                bottom: 10,
                right: 10
            };
            chartWidth = width - (margin.left + margin.right);
            chartHeight = height - (margin.top + margin.bottom);
            svg.attr("width", width).attr("height", height);
            chartLayer
                .attr("width", chartWidth)
                .attr("height", chartHeight)
                .attr("transform", "translate(" + [margin.left, margin.top] + ")");
            return [width,height];
        }

        function drawChart(data,width,height) {
            var color = d3.scaleOrdinal(d3.schemeCategory20);
            var simulation = d3.forceSimulation()
                .force("link", d3.forceLink().id(function(d) {
                        return d.id
                    })
                    .distance(70).strength(1))
                // .force("collide",d3.forceCollide( function(d){return d.r +  }).iterations(16) )
                .force("charge", d3.forceManyBody().strength(function(d){
                  if(d.root)
                      return -2000;
                  return -1000; })
                .distanceMin(50)) 
                .force("center", d3.forceCenter(chartWidth / 2, chartHeight / 2))
                .force("y", d3.forceY(function(d){ 
                  if(d.root)
                    return height/2;
                  return 0;
                }))
                .force("x", d3.forceX(function(d){
                  if(d.root)
                    return width/2;
                  return 0;
                }));
            svg.append("defs").append("marker")
                .attr("id", "arrow")
                .attr("viewBox", "0 -5 10 10")
                .attr("refX", 10)
                .attr("refY", -0.5)
                .attr("markerWidth", 4)
                .attr("markerHeight", 4)
                .attr("orient", "auto")
                .attr("opacity", 0.6)
                .attr("fill",color(8))
                .append("svg:path")
                .attr("d", "M0,-5L10,0L0,5");


            var link = svg.append("g")
                .attr("class", "links")
                .selectAll("line")
                .data(data.links)
                .enter()
                .append("line")
                .attr("stroke", "grey")
                .attr("marker-end", "url(#arrow)");
            var node = svg.append("g")
                .attr("class", "nodes")
                .selectAll("circle")
                .data(data.nodes)
                .enter().append("circle")    
                .attr("r", function(d) {
                    return d.r
                })
                .attr("fill", function(d) {
                    return color(Math.floor(Math.random() * 20));
                });
              
         
            // var linkedByIndex = {};
            //     link.forEach(function(d) {
            //       linkedByIndex[d.source.index + "," + d.target.index] = 1;
            //     });

            var text = svg.append("g").attr("class", "labels").selectAll("g")
                .data(data.nodes)
                .enter().append("g");
            text.append("text")
                .attr("x", 14)
                .attr("y", ".31em")
                .style("font-family", "sans-serif")
                .style("font-size", "0.7em")
                .text(function(d) {
                    return d.label;
                });
            var edgeText = svg.append("g").attr("class", "edgelabels").selectAll("g")
                .data(data.links)
                .enter().append("g");
            edgeText.append("text")
                .attr("x", 14)
                .attr("y", ".31em")
                .style("font-family", "sans-serif")
                .style("font-size", "0.7em")
                .text(function(d) {
                    return d.label;
                });

            var linkedByIndex = {};
            
            link.data().forEach(function (d) {
                linkedByIndex[d.source + "," + d.target] = {index: d.source+","+d.target, id: d.id};
                linkedByIndex[d.target + "," + d.source] = {index: d.source+","+d.target, id: d.id};
            });
            var ticked = function() {

                link
                    .attr("x1", function(d) {
                        return d.source.x;
                    })
                    .attr("y1", function(d) {
                        return d.source.y;
                    })
                    .attr("x2", function(d) {
                        return d.target.x;
                    })
                    .attr("y2", function(d) {
                        return d.target.y;
                    });
                node
                    .attr("cx", function(d) {
                        return d.x;
                    })
                    .attr("cy", function(d) {
                        return d.y;
                    })
                    .classed("example",function(d) {
                        return d3.select(this).datum().root;
                    });
                text
                    .attr("transform", function(d) {
                        return "translate(" + d.x + "," + d.y + ")";
                    });
                edgeText
                    .attr("transform", function(d) {
                        return "translate(" + (d.source.x + d.target.x) / 2 +
                            "," + (d.source.y + d.target.y) / 2 + ")";
                    });
                

            }
            simulation
                .nodes(data.nodes)
                .on("tick", ticked);
            simulation.force("link")
                .links(data.links);

 

        }
    },
    componentDidMount(){
      if (this.state.initialized) {
            return false;
        }
        this.setState({initialized: true});
       this.InitializeGraph(this.props);
        return true;

    },
    componentWillUnmount(){
    },
    componentWillReceiveProps(nextProps) {
        if (this.state.initialized || nextProps.nodes.length == 0) {
            return false;
        }
        this.setState({initialized: true});
       this.InitializeGraph(nextProps);
        return true;
    },

    render() {
        console.log("render presentgraph");

        return ( <
            div ref = 'graph'
            id = "graph"
             >
            </div>
        );
    
    }
});
export default PresentGraph;