{include file="header.tpl"}
{include file="workspace_header.tpl"}

<h1>Set Model Parameters</h1>
<h3>Background on the model </h3>
<p>The BPS analyzer will try to disambiguate among citations using the same name(s). To do this, it will basically model a new citation-person for each name it finds in a document (including fathers, grandfathers, etc. that are mentioned as qualifiers to the named actors). Then, it will attempt to collapse some of those citation-persons to get to the set of actual (real world) persons mentioned in all the corpus documents. Each citation-person is compared to other citation-persons, and a set of rules is applied to determine how likely it is that the two citations are the same person. The analyzer proceeds in two steps: first it considers all the citation-persons within each single document (<em>intra-document</em>), and then it considers the citation-persons across the entire corpus (<em>inter-document</em>). </p>
<p>When comparing two citation-persons, the analyzer will first require that there is no conflicting information about the two citation-persons - e.g., if they have different declared fathers, they will be considered as distinct, and will not be collapsed. The rules below allow you to configure whether specific roles must be considered to be distinct, and to control how strong the likelihood that two persons with partial matching name information are the same real world person. </p>
<div id="contentNarrow">
<br />
<h2>General settings:</h2>
<table cellpadding="10px" class="params" width="100%">
  <tr><td class="paramDesc"><i><b>Number of qualifications (father/grandfather/ancestor/clan) in
      addition to forename required to consider a name citation &quot;fully
      qualified&quot;</b></i></td>
    <td width="150px" class="paramDesc" style="text-align:right;"><input type="text" value="2" size="5" style="text-align:right;"  class="bpsFormInput"/></td>
  </tr>
  <tr><td class="paramDesc"><b><i>Assumed typical length of active business life (years)</i></b></td>
    <td width="150px" class="paramDesc" style="text-align:right;"><input type="text" value="25" size="5" style="text-align:right;"  class="bpsFormInput"/></td>
  </tr>
  <tr><td class="paramDesc"><b><i>Assumed typical separation of generations (years)</i></b></td>
    <td width="150px" class="paramDesc" style="text-align:right;"><input type="text" value="15" size="5" style="text-align:right;"  class="bpsFormInput"/></td>
  </tr>
</table>

<h1>Step 1: Intra-document rules:</h1>


<h3><i>Rule Steps 1A, 1B, and 1C collapse citations within a single document.</i></h3>

<h2>Step 1A: Consider equally qualified names</h2>


<table cellpadding="10px" class="params" width="100%">
  
  <tr>
    <td class="paramDesc"><b>Collapse equal, fully qualified citations&nbsp;<br />
    </b> (e.g., &quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub>,  in-clan </i></b><b><i>CN<sub>c</sub></i></b>&quot;  <br />
    and<strong> </strong>&quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub>,  in-clan CN<sub>c</sub></i></b>&quot;<b><i></i></b>)</td>
    <td class="paramDesc" style="text-align:right;"><select name="select12" class="bpsFormInput">
      <option selected="1">Always: 100%</option>
      <option>Aggressive: 75%</option>
      <option>Moderate: 50%</option>
      <option>Conservative: 30%</option>
      <option>Ignore/No: 0%</option>
    </select></td>
    </tr>
  <tr>
    <td class="paramDesc"><b>Collapse equal, partly qualified citations&nbsp;<br />
    </b> (e.g., &quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub></i></b>&quot;<b><i></i> </b> and<b> </b>&quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub></i></b>&quot;<b><i></i></b>)</td>
    <td class="paramDesc" style="text-align:right;"><select name="select13" class="bpsFormInput">
        <option>Always: 100%</option>
        <option>Aggressive: 75%</option>
        <option>Moderate: 50%</option>
        <option selected="selected">Conservative: 30%</option>
        <option>Ignore/No: 0%</option>
        </select></td>
  </tr>
  
  <tr>
    <td class="paramDesc"><b>Collapse equal, unqualified citations&nbsp;<br />
    </b> (e.g., &quot;<b><i>PN<sub>a</sub></i></b>&quot;<b><i></i> </b> and<b> </b>&quot;<b><i>PN<sub>a</sub></i></b>&quot;<b><i></i></b>)</td>
    <td class="paramDesc" style="text-align:right;"><select name="select13" class="bpsFormInput">
      <option>Always: 100%</option>
      <option selected="selected">Aggressive: 75%</option>
      <option>Moderate: 50%</option>
      <option>Conservative: 30%</option>
      <option>Ignore/No: 0%</option>
    </select></td>
    </tr>
</table>
<h2>Step 1B: Consider compatible, but not equally qualified names</h2>
	<table cellpadding="10px" class="params" width="100%">
  <tr>
    <td class="paramDesc"><b>Collapse partly qualified citations with compatible, fully qualified citations <br />
    </b> (e.g., &quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub></i></b>&quot;<b><i></i> </b> and<b> </b>&quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub>,  in-clan CN<sub>c</sub></i></b>&quot;<b><i></i></b>)</td>
    <td class="paramDesc" style="text-align:right;"><select name="select13" class="bpsFormInput">
        <option>Always: 100%</option>
        <option>Aggressive: 75%</option>
        <option>Moderate: 50%</option>
        <option selected="selected">Conservative: 30%</option>
        <option>Ignore/No: 0%</option>
    </select></td>
  </tr>
  <tr>
    <td class="paramDesc"><b>Collapse unqualified citations with compatible, more qualified citations <br />
    </b> (e.g., &quot;<b><i>PN<sub>a</sub></i></b>&quot;<b><i></i> </b> and<b> </b>&quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub>,  in-clan CN<sub>c</sub></i></b>&quot;<b><i></i></b>,<br />
    OR, 
 &quot;<b><i>PN<sub>a</sub></i></b>&quot;<b><i></i> </b> and<b> </b>&quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub></i></b>&quot;)</td>
    <td class="paramDesc" style="text-align:right;"><select name="select13" class="bpsFormInput">
        <option>Always: 100%</option>
        <option selected="selected">Aggressive: 75%</option>
        <option>Moderate: 50%</option>
        <option>Conservative: 30%</option>
        <option>Ignore/No: 0%</option>
    </select></td>
  </tr>
</table>
<h2>Step 1C: Consider the roles of persons </h2>
<p>Note that &quot;ancestors&quot; includes all fathers, mothers, grandfathers, and other declared ancestors. </p>
<table cellpadding="10px" class="params" width="100%">
  <tr><td colspan="4" class="caseTitle">
      <h3><b>Can two instances of the same name within </b>a document possibly be the same, just given the associated roles for the two names? </h3>
    </td>
  </tr>
  <tr>
    <td class="paramTitle" ></td>
    <td class="paramTitle" >Principle</td>
    <td class="paramTitle" >Witness </td>
    <td class="paramTitle" >Ancestor </td>
  </tr>
  <tr class="paramDesc">
    <td class="paramDesc" >Principle</td>
    <td class="paramData" ><select class="bpsFormInput">
			<option selected="selected" value="1">Always: 100%</option>
			<option value="0.75">Usually: 80%</option>
			<option value="0.5">Sometimes: 50%</option>
			<option value="0.3">Rarely: 20%</option>
			<option value="0">Never: 0%</option>
		</select></td>
    <td class="paramData" >
			<select name="select" class="bpsFormInput">
			<option value="1">Always: 100%</option>
			<option value="0.75">Usually: 80%</option>
			<option value="0.5">Sometimes: 50%</option>
			<option value="0.3">Rarely: 20%</option>
			<option selected="selected" value="0">Never: 0%</option>
    </select></td>
    <td class="paramData" ><select name="select4" class="bpsFormInput">
			<option selected="selected" value="1">Always: 100%</option>
			<option value="0.75">Usually: 80%</option>
			<option value="0.5">Sometimes: 50%</option>
			<option value="0.3">Rarely: 20%</option>
			<option value="0">Never: 0%</option>
    </select></td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Witness</td>
    <td class="paramDesc" >&nbsp;</td>
    <td class="paramData" ><select name="select2" class="bpsFormInput">
			<option value="1">Always: 100%</option>
			<option value="0.75">Usually: 80%</option>
			<option value="0.5">Sometimes: 50%</option>
			<option value="0.3">Rarely: 20%</option>
			<option selected="selected" value="0">Never: 0%</option>
    </select></td>
    <td class="paramData" ><select name="select5" class="bpsFormInput">
			<option selected="selected" value="1">Always: 100%</option>
			<option value="0.75">Usually: 80%</option>
			<option value="0.5">Sometimes: 50%</option>
			<option value="0.3">Rarely: 20%</option>
			<option value="0">Never: 0%</option>
    </select></td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Ancestor</td>
    <td class="paramDesc" >&nbsp;</td>
    <td class="paramDesc" >&nbsp;</td>
    <td class="paramData" ><select name="select3" class="bpsFormInput">
			<option selected="selected" value="1">Always: 100%</option>
			<option value="0.75">Usually: 80%</option>
			<option value="0.5">Sometimes: 50%</option>
			<option value="0.3">Rarely: 20%</option>
			<option value="0">Never: 0%</option>
    </select></td>
  </tr>
</table>

  <h1>Step 2: Inter-document rules:</h1>
  <h3><i>Rule Steps 2A, 2B, and 2C collapse citations across all documents.</i></h3>

  <h2>Step 2A: Consider equally qualified names</h2>
  <table cellpadding="10px" class="params" width="100%">
    <tr>
      <td class="paramDesc"><b>Collapse equal, fully qualified citations&nbsp;<br />
      </b> (e.g., &quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub>,  in-clan </i></b><b><i>CN<sub>c</sub></i></b>&quot;<b><i></i> </b> <br />
      and<b> </b>&quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub>,  in-clan CN<sub>c</sub></i></b>&quot;<b><i></i></b>)</td>
      <td class="paramDesc" style="text-align:right;"><select name="select14" class="bpsFormInput">
          <option selected="selected">Always: 100%</option>
          <option>Aggressive: 75%</option>
          <option>Moderate: 50%</option>
          <option>Conservative: 30%</option>
          <option>Ignore/No: 0%</option>
      </select></td>
    </tr>
    <tr>
      <td class="paramDesc"><b>Collapse equal, partly qualified citations&nbsp;<br />
      </b> (e.g., &quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub></i></b>&quot;<b><i></i> </b> and<b> </b>&quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub></i></b>&quot;<b><i></i></b>)</td>
      <td class="paramDesc" style="text-align:right;"><select name="select14" class="bpsFormInput">
          <option>Always: 100%</option>
          <option selected="selected">Aggressive: 75%</option>
          <option>Moderate: 50%</option>
          <option>Conservative: 30%</option>
          <option>Ignore/No: 0%</option>
            </select></td>
    </tr>
    <tr>
      <td class="paramDesc"><b>Collapse equal, unqualified citations&nbsp;<br />
      </b> (e.g., &quot;<b><i>PN<sub>a</sub></i></b>&quot;<b><i></i> </b> and<b> </b>&quot;<b><i>PN<sub>a</sub></i></b>&quot;<b><i></i></b>)</td>
      <td class="paramDesc" style="text-align:right;"><select name="select14" class="bpsFormInput">
          <option>Always: 100%</option>
          <option>Aggressive: 75%</option>
          <option selected="selected">Moderate: 50%</option>
          <option>Conservative: 30%</option>
          <option>Ignore/No: 0%</option>
            </select></td>
    </tr>
  </table>
  <h2>Step 2B: Consider compatible, but not equally qualified names</h2>
  <table cellpadding="10px" class="params" width="100%">
    <tr>
      <td class="paramDesc"><b>Collapse partly qualified citations with compatible, fully qualified citations <br />
      </b> (e.g., &quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub></i></b>&quot;<b><i></i> </b> and<b> </b>&quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub>,  in-clan CN<sub>c</sub></i></b>&quot;<b><i></i></b>)</td>
      <td class="paramDesc" style="text-align:right;"><select name="select14" class="bpsFormInput">
          <option>Always: 100%</option>
          <option selected="selected">Aggressive: 75%</option>
          <option>Moderate: 50%</option>
          <option>Conservative: 30%</option>
          <option>Ignore/No: 0%</option>
            </select></td>
    </tr>
    <tr>
      <td class="paramDesc"><b>Collapse unqualified citations with compatible, more qualified citations <br />
        </b> (e.g., &quot;<b><i>PN<sub>a</sub></i></b>&quot;<b><i></i> </b> and<b> </b>&quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub>,  in-clan CN<sub>c</sub></i></b>&quot;<b><i></i></b>,<br />
        OR, 
        &quot;<b><i>PN<sub>a</sub></i></b>&quot;<b><i></i> </b> and<b> </b>&quot;<b><i>PN<sub>a</sub>, son-of PN<sub>b</sub></i></b>&quot;)</td>
      <td class="paramDesc" style="text-align:right;"><select name="select14" class="bpsFormInput">
          <option>Always: 100%</option>
          <option>Aggressive: 75%</option>
          <option>Moderate: 50%</option>
          <option selected="selected">Conservative: 30%</option>
          <option>Ignore/No: 0%</option>
            </select></td>
    </tr>
  </table>
  <h2>Step 2C: Consider the roles of persons </h2>
  <p>Note that &quot;ancestors&quot; includes all fathers, mothers, grandfathers, and other declared ancestors. </p>
  <table cellpadding="10px" class="params" width="100%">
  <tr><td colspan="4" class="caseTitle">
      <h3><b>Can two instances of the same name in different </b>documents possibly be the same, just given the associated roles for the two names? </h3>
    </td>
  </tr>
  <tr>
    <td class="paramTitle" ></td>
    <td class="paramTitle" >Principle</td>
    <td class="paramTitle" >Witness </td>
    <td class="paramTitle" >Ancestor </td>
  </tr>
  <tr>
    <td class="paramDesc" >Principle</td>
    <td class="paramDesc" ><select name="select6" class="bpsFormInput">
      <option selected="selected" value="1">Always: 100%</option>
      <option value="0.75">Usually: 80%</option>
      <option value="0.5">Sometimes: 50%</option>
      <option value="0.3">Rarely: 20%</option>
      <option value="0">Never: 0%</option>
    </select></td>
    <td class="paramDesc" ><select name="select7" class="bpsFormInput">
      <option selected="selected" value="1">Always: 100%</option>
      <option value="0.75">Usually: 80%</option>
      <option value="0.5">Sometimes: 50%</option>
      <option value="0.3">Rarely: 20%</option>
      <option value="0">Never: 0%</option>
    </select></td>
    <td class="paramDesc" ><select name="select9" class="bpsFormInput">
      <option selected="selected" value="1">Always: 100%</option>
      <option value="0.75">Usually: 80%</option>
      <option value="0.5">Sometimes: 50%</option>
      <option value="0.3">Rarely: 20%</option>
      <option value="0">Never: 0%</option>
    </select></td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Witness</td>
    <td class="paramDesc" >&nbsp;</td>
    <td class="paramDesc" ><select name="select8" class="bpsFormInput">
      <option selected="selected" value="1">Always: 100%</option>
      <option value="0.75">Usually: 80%</option>
      <option value="0.5">Sometimes: 50%</option>
      <option value="0.3">Rarely: 20%</option>
      <option value="0">Never: 0%</option>
    </select></td>
    <td class="paramDesc" ><select name="select10" class="bpsFormInput">
      <option selected="selected" value="1">Always: 100%</option>
      <option value="0.75">Usually: 80%</option>
      <option value="0.5">Sometimes: 50%</option>
      <option value="0.3">Rarely: 20%</option>
      <option value="0">Never: 0%</option>
    </select></td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Ancestor</td>
    <td class="paramDesc" >&nbsp;</td>
    <td class="paramDesc" >&nbsp;</td>
    <td class="paramDesc" ><select name="select11" class="bpsFormInput">
      <option selected="selected" value="1">Always: 100%</option>
      <option value="0.75">Usually: 80%</option>
      <option value="0.5">Sometimes: 50%</option>
      <option value="0.3">Rarely: 20%</option>
      <option value="0">Never: 0%</option>
    </select></td>
  </tr>
</table>
</div>
{include file="footer.tpl"}
