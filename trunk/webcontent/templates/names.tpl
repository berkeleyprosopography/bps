{include file="header.tpl"}

{if isset($errmsg) }
	<h2>{$errmsg}</h2>
{elseif !isset($document) }
	<h2>No document specified!</h2>
{elseif !isset($corpus) }
	<h2>No corpus specified!</h2>
{else}
	<p class="nav-right">
		<a href="/corpora/corpus?id={$corpusID}">Return to Corpus details</a>
	</p>
	{if empty($names)}
		<h1>No {$type}-Name information available for corpus</h1>
	{else}
		<h1>{$names|@count} {$type}-Names in Corpus:</h1>
		<p class="nav-right">
			<span class="filterLabel">Filter by Role:&nbsp;</span>
			<select id="RoleFilterSel">
				<option value="0" selected="true">Show All</option>
				{section name=irole loop=$roles}
					<option value="{$roles[irole].id}">{$roles[irole].name}</option>
				{/section}
			</select>
			<input id="goBtn" type="button" value="Go" onclick="filterNames({$corpusID});" />
		</p>
		<table class="nrads_row" border="0" cellspacing="0" cellpadding="4px" width="100%">
			<tr>
				<td class="title" width="200px">Name</td>
				<td class="title" width="140px"># Documents</td>
				<td class="title" width="140px">Total Instances</td>
			</tr>
			{section name=iname loop=$names}
				<tr>
					<td class="name" style="padding-top:6px">
						{$names[iname].name}
					</td>
					<td class="name" style="padding-top:6px">
						{$names[iname].ndocs}
					</td>
					<td class="name" style="padding-top:6px">
						{$names[iname].total}
					</td>
				</tr>
			{/section}
		</table>
	{/if}
	<p>&nbsp;</p>
	<p class="nav-right">
		<a href="/corpora/corpus?id={$corpusID}">Return to Corpus details</a>
	</p>
{/if}

{include file="footer.tpl"}
