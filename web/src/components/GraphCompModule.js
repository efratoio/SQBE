import * as d3 from 'd3'
import React, {
    Component
} from 'react';
import ReactDOM from 'react-dom';
import conf from "./constants";
import AutoComplete from 'material-ui/AutoComplete';
import RaisedButton from 'material-ui/RaisedButton';

const GraphComp = React.createClass({

   getInitialState () {
    return {
      initialized: false,
    };
  },
    propTypes: {
        links: React.PropTypes.array,
        nodes: React.PropTypes.array,
        example: React.PropTypes.number,
        onAddNode: React.PropTypes.func,
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
        // var chartLayer = svg.append("g").classed("chartLayer", true)
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
            }),false);
   
        function main(el, n, l,b) {
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
            drawChart(data,size[0],size[1],b);
        }

        function setSize( el) {
            width = 1000;//ReactDOM.findDOMNode(el).getBoundingClientRect().width;
            height = 1000;//ReactDOM.findDOMNode(el).getBoundingClientRect().height;
            // margin = {
            //     top: 10,
            //     left: 50,
            //     bottom: 10,
            //     right: 10
            // };
            // chartWidth = width - (margin.left + margin.right);
            // chartHeight = height - (margin.top + margin.bottom);
            svg.attr("width", width).attr("height", height);
           


            return [width,height];
        }

        function drawChart(data,width,height,filter) {
            var color = d3.scaleOrdinal(d3.schemeCategory20);

            // var zoom = d3.zoom()
            //     .scaleExtent([1, 40])
            //     .translateExtent([[-100, -100], [width + 90, height + 100]])
            //     .on("zoom", zoomed);

            // var x = d3.scaleLinear()
            //     .domain([-1, width + 1])
            //     .range([-1, width + 1]);

            // var y = d3.scaleLinear()
            //     .domain([-1, height + 1])
            //     .range([-1, height + 1]);

            // var xAxis = d3.axisBottom(x)
            //     .ticks((width + 2) / (height + 2) * 10)
            //     .tickSize(height)
            //     .tickPadding(8 - height);

            // var yAxis = d3.axisRight(y)
            //     .ticks(10)
            //     .tickSize(width)
            //     .tickPadding(8 - width);

            // var view = svg.append("rect")
            //     .attr("class", "view")
            //     .attr("x", 0.5)
            //     .attr("y", 0.5)
            //     .attr("width", width - 1)
            //     .attr("height", height - 1);

            // var gX = svg.append("g")
            //     .attr("class", "axis axis--x")
            //     .call(xAxis);

            // var gY = svg.append("g")
            //     .attr("class", "axis axis--y")
            //     .call(yAxis);

            // svg.call(zoom);

            // function zoomed() {
            //   view.attr("transform", d3.event.transform);
            //   gX.call(xAxis.scale(d3.event.transform.rescaleX(x)));
            //   gY.call(yAxis.scale(d3.event.transform.rescaleY(y)));
            // }

            // function resetted() {
            //   svg.transition()
            //       .duration(750)
            //       .call(zoom.transform, d3.zoomIdentity);
            // }
            var simulation = d3.forceSimulation()
                .force("link", d3.forceLink().id(function(d) {
                        return d.id
                    })
                    .distance(120).strength(1))
                // .force("collide",d3.forceCollide( function(d){return d.r +  }).iterations(16) )
                .force("charge", d3.forceManyBody().strength(function(d){
               
                  return -1000; })
                .distanceMin(120)) 
                .force("center", d3.forceCenter(height/2, width/2))
                .force("y", d3.forceY(function(d){ 
                  if(d.root)
                    return height/4;
                  return 0;
                }))
                .force("x", d3.forceX(function(d){
                  if(d.root)
                    return width/4;
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
                .attr("d", "M0,-5L10,0L0,5")
                .style("pointer-events", "all");
          


            var link = svg.append("g")
                .attr("class", "links")
                .selectAll("line")
                .data(data.links)
                .enter()
                .append("line")
                .attr("stroke", "grey")
                .attr("marker-end", "url(#arrow)")
                .on("click", handleMouseClickEdge);

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
                })
                .call(d3.drag()
                    .on("start", dragstarted)
                    .on("drag", dragged)
                    .on("end", dragended))
                .on("mouseover", handleMouseOver)
                .on("mouseout", handleMouseOut)
                .on("click", handleMouseClick);
              
         
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
                .style("font-color","#666699")
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
                    })
                    .classed("fixed",function(d) {
                        return d3.select(this).attr("chosen");
                    })
                    .classed("chosen",function(d) {
                        return d3.select(this).attr("chosen");
                    })
                    .classed("disabled",function(d){
                        return d3.select(this).attr("disabled");
                    })
                    .classed("fixed",function(d){
                        return d3.select(this).attr("disabled");
                    })
                    .classed("perm",function(d){
                        return d3.select(this).attr("perm");
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
                    })
                    .classed("fixed",function(d) {
                        return d3.select(this).attr("chosen");
                    })
                    .classed("chosen",function(d) {
                        return d3.select(this).attr("chosen");
                    })
                    .classed("disabled",function(d){
                        return d3.select(this).attr("disabled");
                    })
                    .classed("fixed",function(d){
                        return d3.select(this).attr("disabled");
                    });
                text
                    .attr("transform", function(d) {
                        return "translate(" + d.x + "," + d.y + ")";
                    });
                edgeText
                    .attr("transform", function(d) {
                        if(d.source.x<d.target.x){
                            return "translate(" + (3*d.source.x + d.target.x - 20) / 4 +
                                "," + (d.source.y + d.target.y) / 2 + ")";
                        }
                       return "translate(" + (d.source.x + 3*d.target.x -20 ) / 4 +
                            "," + (d.source.y + d.target.y) / 2 + ")";
                    });
                

            }
            simulation
                .nodes(data.nodes)
                .on("tick", ticked);
            simulation.force("link")
                .links(data.links);

            function zoomed() {
                
              g.attr("transform", d3.event.transform);
            }

            var zoom = d3.zoom()
                .scaleExtent([1, 40])
                .translateExtent([[-100, -100], [width + 90, height + 100]])
                .on("zoom", zoomed);
            // function zoomed() {
            //   var transform = d3.event.transform;
            //   node.attr("transform", function(d) {
            //     return "translate(" + d.x + "," + d.y + ")";
            // });
            //   link.attr("transform", function(d) {
            //     return "translate(" + d.source.x + "," + d.source.y + ")";
            // });
            //   text.attr("transform", function(d) {
            //     return "translate(" + d.x + "," + d.y + ")";
            // });              
            //   edgeText.attr("transform", function(d) {
            //     return "translate(" + (d.source.x + d.target.x) / 2 + "," + (d.source.y + d.target.y) / 2 + ")";
            // });
            // }

            function dragstarted(d) {
              if(!d3.select(this).attr("disabled")){
                if (!d3.event.active) simulation.alphaTarget(0.3).restart();
                d.fx = d.x;
                d.fy = d.y;
              }
            }

            function dragged(d) {
              if(!d3.select(this).attr("disabled")){
                d.fx = d3.event.x;
                d.fy = d3.event.y;
              }
            }

            function dragended(d) {
              if(!d3.select(this).attr("disabled")){
                if (!d3.event.active) simulation.alphaTarget(0);
                d.fx = null;
                d.fy = null;
              }
            }
            // Create Event Handlers for mouse
            function handleMouseOver(d, i) { // Add interactivity
                // Use D3 to select element, change color and size
                d3.select(this)
                    .attr("fill", "orange")
                    .attr("r",radius+2 );
            }

         

            function handleMouseOut(d, i) {
                // Use D3 to select element, change color back to normal
                
                if(d.fixed){  
                  d3.select(this)
                  .attr("r",radius-3);
                  return;
                }
                d3.select(this)
                    .attr("fill", color(Math.floor(Math.random() * 20)))
                    .attr("r", radius);
            }
            function handleMouseClick(d, i) {
                handlePaths(d);
            }
            function undo(v){
                    link.attr("chosen",null).attr("disabled",null).attr("path",null).style("stroke","grey")
                  .each((d)=>d.path=null);
                  node.attr("chosen",null).attr("disabled",null);

            }
            function handlePaths(d) {
                // Use D3 to select element, change color back to normal
                const paths = getPath(nextProps.example,d.id);
                const color = d3.scaleOrdinal(d3.schemeCategory10);

                node.attr("disabled",true);
                link.attr("disabled",true).each((d)=>d.path=new Set());

                var j=0;
                paths.forEach(function(path){

                    path = JSON.parse(path);
                    node
                    .filter(function(d){return path.includes(d.id);})
                    .attr("chosen",true)
                    .attr("disabled",null);
                    for (let i = 0; i < path.length-1; i++) {

                          link
                          .filter(function(d)
                           { return d.source.id+","+d.target.id == linkedByIndex[path[i]+","+path[i+1]].index; })
                          .style("stroke",color(j))                      
                          .attr("chosen",true)
                          .attr("disabled",null)
                          .each((d)=>d.path.add(j));
                      }
                    j+=1;
                    j = j%10;
                });
                choosePhase = true;
                
                nextProps.onAddNode(d.id);
            }
            function handleMouseClickEdge(d, i) {
                if(choosePhase && d.path.size>=0){
                  var pathNum = d.path.values().next().value;
                  
                  nextProps.onAddEdge(link.filter((d)=>d.path.has(pathNum))
                    .attr("perm",true).data().map((x)=>x.id));
                  //choose phase is over
                  link.attr("chosen",null).attr("disabled",null).attr("path",null).style("stroke","grey")
                  .each((d)=>d.path=null);
                  node.attr("chosen",null).attr("disabled",null);

                  choosePhase=false;
                }


            }
            
            function getNeighbours(a){
              return node.filter(function(d){ return linkedByIndex[a+","+d.id]; })
              .data().map((x)=>x.id);
            }

            function getPath(a,b){
              var paths = new Set();
              var records = [{path:[a,],
                track : node.data().map(function(y){
                  return { id :y.id, visited : (y.id == a) };}),del : false}];
              let i=0;
              do{
                
                i+=1;
                
                records.filter((arr)=>arr.path[arr.path.length-1]==b)
                .forEach((arr)=> paths.add(JSON.stringify(arr.path)));
                records =records.filter((arr)=>arr.path[arr.path.length-1]!=b || !arr.del);
                var newRecords = [];
                records.forEach(function(o){
                 var n = getNeighbours(o.path[o.path.length-1],b);
                    n.forEach(function(nei){
                      var newPath = o.path.slice(0);
                      newPath.push(nei);
                      if(!o.track.filter((x)=>x.visited).map((x)=>x.id).includes(nei)){
                        newRecords.push({path:newPath, 
                          track : o.track.map(function(x){
                            return {id:x.id,visited:(x.visited||x.id==nei)};}), del : false})
                      }
                  });
                    o.del = true;
                    });
                records = Array.concat(records,newRecords);
              }while(i<6 && records.length>0);
              return [...paths];
            }
        }
    },
    onNewRequest(searchNode){
         this.refs[`autocomplete`].setState({searchText:''});
         this.refs[`autocomplete`].focus();
         var f = function(d){return d.id==searchNode.value;}
        d3.selectAll("circle").filter((d)=>d).filter(f).on("click")(d3.selectAll("circle").filter((d)=>d).filter(f));
               
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
        const arr = this.props.nodes.map(function(x){
            var obj =  {text:x.label,value:x.id};
            return obj;
        });
        return ( <div>
        <AutoComplete hintText={"type here..."}
        ref={`autocomplete`}
        fullWidth={true}
        dataSource    = {arr}
        onNewRequest = {this.onNewRequest} />
            <
            div ref = 'graph'
            id = "graph"
            >
            <
            /div><RaisedButton onClick={(e)=>this.undo(e)}>UNDO</RaisedButton></div>
        );
    }
});
export default GraphComp;