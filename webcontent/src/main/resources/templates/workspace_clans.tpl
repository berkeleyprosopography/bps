{include file="header.tpl"}
{include file="workspace_header.tpl"}

{if !isset($workspace) }
	<h1>Error: No workspace specified!</h1>
{else}
	{if isset($errmsg) }
		<h2>{$errmsg}</h2>
	{else}
		{if empty($clans)}
			<h1><i>No</i> Clans (yet found) in Workspace</h1>
			<p><i>Perhaps you need to regenerate entities for the workspace? <br>
					(Yes, this is irritating, and we will fix it soon)</i></p>
		{else}
			<h1>Showing {$clans|@count} Clans for Workspace:</h1>
			<table class="clans_row" border="0" cellspacing="0" cellpadding="4px" width="100%">
				<tr>
					<td class="title" width="160px">
						<a href="/workspace?wid={$wkspId}&view=clans&o=name">Name</a>
					</td>
				</tr>
				{section name=clan loop=$clans}
					<tr>
						<td class="clan" style="padding-top:6px">{$clans[clan].displayName}</td>
					</tr>
				{/section}
			</table>
		{/if}
	{/if}
	<p>&nbsp;</p>
	{if isset($opmsg) }
		<p id="statusP">{$opmsg}</p>
	{else}
		<p id="statusP"> &nbsp; </p>
	{/if}
{/if}


{include file="footer.tpl"}

