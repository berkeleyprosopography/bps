{include file="header.tpl"}
{include file="workspace_header.tpl"}

<h1>Set Model Parameters for Workspace: {$workspace.name}</h1>
<h3>Background on the model </h3>
<p>The BPS analyzer will try to disambiguate among citations using the same name(s). To do this, it will basically model a new citation-person for each name it finds in a document (including fathers, grandfathers, etc. that are mentioned as qualifiers to the named actors). Then, it will attempt to collapse some of those citation-persons to get to the set of actual (real world) persons mentioned in all the corpus documents. Each citation-person is compared to other citation-persons, and a set of rules is applied to determine how likely it is that the two citations are the same person. The analyzer proceeds in two steps: first it considers all the citation-persons within each single document (<em>intra-document</em>), and then it considers the citation-persons across the entire corpus (<em>inter-document</em>). </p>
<p>When comparing two citation-persons, the analyzer will first require that there is no conflicting information about the two citation-persons - e.g., if they have different declared fathers, they will be considered as distinct, and will not be collapsed. The rules below allow you to configure whether specific roles must be considered to be distinct, and to control how strong the likelihood that two persons with partial matching name information are the same real world person. </p>
{if isset($workspace_error) }
  <h2>{$workspace_error}</h2>
{else}
  <div id="contentNarrow">
  <br />
  <h2>General settings:</h2>
  <table cellpadding="10px" class="params" width="100%">
    <tr><td class="paramDesc"><i><b>Number of qualifications (father/grandfather/ancestor/clan) in
        addition to forename required to consider a name citation &quot;fully
        qualified&quot;</b></i></td>
      <td width="150px" class="paramDesc" style="text-align:right;"><input type="text" value="2" size="5" style="text-align:right;" disabled="disabled" class="bpsFormInput"/></td>
    </tr>
    <tr><td class="paramDesc"><b><i>Assumed typical length of active business life (years)</i></b></td>
      <td width="150px" class="paramDesc" style="text-align:right;">
          <input id="I_ALife" type="text" value="{$workspace.activeLife}" size="5"
              style="text-align:right;"  class="bpsFormInput" onkeyup="enableElement('U_gen',true)"/>
      </td>
    </tr>
    <tr><td class="paramDesc"><b><i>Assumed typical separation of generations (years)</i></b></td>
      <td width="150px" class="paramDesc" style="text-align:right;">
          <input id="I_GenSep" type="text" value="{$workspace.generationOffset}" size="5" style="text-align:right;"
              class="bpsFormInput" onkeyup="enableElement('U_gen',true)"/>
      </td>
    </tr>
    <tr>
      <td class="paramDesc"></td>
      <td width="150px" class="paramDesc" style="text-align:right;">
          <input disabled id="U_gen" type="button" value=" Update " size="5" style="text-align:right;" 
                  class="bpsFormInput" onclick="updateGenParams('{$workspace.id}','I_ALife','I_GenSep')" />
      </td>
    </tr>
  </table>

  {if !isset($collapser.intra_groups) }
    <p>There are no intraDocument rule groups defined in the system!</p>
  {else}
    <br/>
    <h1>Step 1: Intra-document rules:</h1>
    <h3><i>These rules collapse citations within a single document.</i></h3>

    {* Loop over the intra-doc groups *}
    {section name=group loop=$collapser.intra_groups}
      <h2>{$collapser.intra_groups[group].header}</h2>
      <table cellpadding="10px" class="params" width="100%">
      {if !isset($collapser.intra_groups[group].rules) }
        <p>There are no rules defined in this group!</p>
      {else}
        {* Loop over each rule in the group *}
        {section name=rule loop=$collapser.intra_groups[group].rules}
          {if isset($collapser.intra_groups[group].rules[rule].matrixAxisValues) }
            {* Row with the description spanning all columns *}
            <tr>
              {section name=mAxisVDummy loop=$collapser.intra_groups[group].rules[rule].matrixAxisValues}
                {if $smarty.section.mAxisVDummy.first}
                  <td class="paramDesc" colspan="{$smarty.section.mAxisVDummy.total+1}">
                      {$collapser.intra_groups[group].rules[rule].description}</td>
                {/if}
              {/section}
            </tr>
            {* Row with the column names *}
            <tr>
              {* Empty item for the row names column *}
              <td class="paramDesc"></td>
              {* Loop over the columns backwards for the columns labels *}
              {section start=-1 step=-1 name=column loop=$collapser.intra_groups[group].rules[rule].matrixAxisValues}
                <td class="paramDesc colName" >
                    {$collapser.intra_groups[group].rules[rule].matrixAxisValues[column]}</td>
              {/section}
            </tr>
            {* Now, the rows proper. Loop over the axis names, and produce a row for each *}
            {section name=row loop=$collapser.intra_groups[group].rules[rule].matrixAxisValues}
              <tr>
                {assign var='currRowVal' value=`$collapser.intra_groups[group].rules[rule].matrixAxisValues[row]`}
                {assign var='currRowValL' value=`$collapser.intra_groups[group].rules[rule].matrixAxisValuesLower[row]`}
                <td class="paramDesc rowName" >{$currRowVal}</td>
                {section start=-1 step=-1 name=col loop=$collapser.intra_groups[group].rules[rule].matrixAxisValues}
                  {assign var='currColVal' value=`$collapser.intra_groups[group].rules[rule].matrixAxisValues[col]`}
                  {assign var='currColValL' value=`$collapser.intra_groups[group].rules[rule].matrixAxisValuesLower[col]`}
                  <td class="paramDesc" style="text-align:right;">
                    {if isset($collapser.intra_groups[group].rules[rule].matrixItems[$currRowValL][$currColValL])}
                      <select name="select1_{$currRowVal}_{$currColVal}" class="bpsFormInput"
                          onchange="updateMatrixRuleWeight('{$workspace.id}',
                                      '{$collapser.intra_groups[group].rules[rule].name}',
                                      '{$currRowVal}','{$currColVal}',this.value);" >
                        {section name=userWeight loop=$collapser.intra_groups[group].rules[rule].userWeights}
                          <option 
                            value="{$collapser.intra_groups[group].rules[rule].userWeights[userWeight].weight}"
                            {if $collapser.intra_groups[group].rules[rule].matrixItems[$currRowValL][$currColValL]
                              == $collapser.intra_groups[group].rules[rule].userWeights[userWeight].weight }
                               selected="1" 
                            {/if} >
                           {$collapser.intra_groups[group].rules[rule].userWeights[userWeight].label}
                          </option>
                        {/section}
                      </select>
                    {else}
                    {* Empty cell *}
                    {/if}
                  </td>
                {/section}
              </tr>
            {/section}
          {else}
            <tr>
                <td class="paramDesc">{$collapser.intra_groups[group].rules[rule].description}</td>
                <td class="paramDesc" style="text-align:right;">
                  <select name="select1_{$collapser.intra_groups[group].name}_{$collapser.intra_groups[group].rules[rule].name}"
                      class="bpsFormInput"
                      onchange="updateSimpleRuleWeight('{$workspace.id}',
                                  '{$collapser.intra_groups[group].rules[rule].name}',this.value);" >
                    {section name=userWeight loop=$collapser.intra_groups[group].rules[rule].userWeights}
                      <option 
                        value="{$collapser.intra_groups[group].rules[rule].userWeights[userWeight].weight}"
                        {if $collapser.intra_groups[group].rules[rule].weight
                          == $collapser.intra_groups[group].rules[rule].userWeights[userWeight].weight }
                           selected="1" 
                        {/if} >
                       {$collapser.intra_groups[group].rules[rule].userWeights[userWeight].label}
                      </option>
                    {/section}
                  </select>
                </td>
            </tr>
          {/if}
        {/section}
      {/if}
      </table>
    {/section}
  {/if}

  {if !isset($collapser.inter_groups) }
    <p>There are no interDocument rule groups defined in the system!</p>
  {else}
    <br/>
    <h1>Step 2: Inter-document rules:</h1>
    <h3><i>These rules collapse citations across all documents.</i></h3>

    {* Loop over the inter-doc groups *}
    {section name=group loop=$collapser.inter_groups}
      <h2>{$collapser.inter_groups[group].header}</h2>
      <table cellpadding="10px" class="params" width="100%">
      {if !isset($collapser.inter_groups[group].rules) }
        <p>There are no rules defined in this group!</p>
      {else}
        {* Loop over each rule in the group *}
        {section name=rule loop=$collapser.inter_groups[group].rules}
          <tr>
              <td class="paramDesc">{$collapser.inter_groups[group].rules[rule].description}</td>
              <td class="paramDesc" style="text-align:right;">
                <select name="select1_{$collapser.inter_groups[group].name}_{$collapser.inter_groups[group].rules[rule].name}"
                    class="bpsFormInput"
                    onchange="updateSimpleRuleWeight('{$workspace.id}',
                                '{$collapser.inter_groups[group].rules[rule].name}',this.value);" ">
                  {section name=userWeight loop=$collapser.inter_groups[group].rules[rule].userWeights}
                    <option 
                      value="{$collapser.inter_groups[group].rules[rule].userWeights[userWeight].weight}"
                      {if $collapser.inter_groups[group].rules[rule].weight
                        == $collapser.inter_groups[group].rules[rule].userWeights[userWeight].weight }
                         selected="1" 
                      {/if}>
                     {$collapser.inter_groups[group].rules[rule].userWeights[userWeight].label}
                    </option>
                  {/section}
                </select>
              </td>
          </tr>
        {/section}
      {/if}
      </table>
    {/section}
  {/if}

  </div>
{/if}
{include file="footer.tpl"}
