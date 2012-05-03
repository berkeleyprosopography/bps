{include file="header.tpl"}
{include file="workspace_header.tpl"}

{if !isset($workspace) }
	<h1>Error: No workspace specified!</h1>
{else}
	{if isset($errmsg) }
		<h2>{$errmsg}</h2>
	{else}
		{if empty($documents)}
			<h1><i>No</i> Documents (yet) in Workspace</h1>
		{else}
			<h1>Showing {$documents|@count} Documents in Workspace:</h1>
			<table class="docs_row" border="0" cellspacing="0" cellpadding="4px" width="100%">
				<tr>
					<td class="title" width="160px">
						<a href="/workspace?&o=altId">Document</a>
					</td>
					<td class="title" width="160px">Publication</td>
					<td class="title" width="350px">Notes</td>
					<td class="title" width="100px">
						<a href="/workspace?&o=date">Date</a>
					</td>
				</tr>
				{section name=doc loop=$documents}
					<tr>
						<td class="document" style="padding-top:6px">
							<a href="/workspace/document?wid={$workspace.id}&did={$documents[doc].id}">{$documents[doc].alt_id}</a></td>
						<td class="document" style="padding-top:6px">{$documents[doc].primaryPubl}</td>
						<td class="document" style="padding-top:6px">{$documents[doc].notes}</td>
						<td class="document" style="padding-top:6px">{$documents[doc].date_str}</td>
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
