{include file="header.tpl"}

 <div class="admin_hdr" >
	<p id="orient"><a href="admin.php">Admin Main</a> -> Edit Role Permissions</p>
 </div>

{if isset($perm_error) }
	<h2>{$perm_error}</h2>
{else}
	{if !isset($roles) }
		<p>There are no roles defined!</p>
	{else}
		{if !isset($permroles) }
			<p>There are no roles defined!</p>
		{else}
	<table border="0" cellspacing="0" cellpadding="3">
		<tr>
			<td class="title" align="left"><em>Permission</em></td>
			{section name=role loop=$roles}
				<td class="title" width="100px" align="center">{$roles[role]}</td>
			{/section}
			{foreach key=kperm item=vroles from=$permroles }
				<tr><td class="role" ><p><strong>{$kperm}</strong></p></td>
				{foreach item=role from=$roles}
					<td class="role" align="center">
						<input type="checkbox" class="set" id="{$kperm}.{$role}"
						{if isset($vroles[$role]) }
							checked="true"
						{/if}
						onclick="MarkChanged(event);" />
					</td>
				{/foreach}
				</tr>
			{/foreach}
		</tr>
	</table>
		
		{/if}
	{/if}
{/if}
{include file="footer.tpl"}
