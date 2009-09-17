{include file="header.tpl"}

<h1>{$username}</h1>
<div class="profile_memberSince">Member since {$creation_time|date_format}</div>
<p>{if $ownProfile}<a href="{$wwwroot}/modules/auth/profileEdit.php">Edit my profile</a>{/if}</p>

{if $real_name}
<div class="profile_metadata">
	<span class="profile_metadataLabel">Name:</span> {$real_name}
</div>
{/if}
{if $website_url}
<div class="profile_metadata">
	<span class="profile_metadataLabel">Website:</span> <a href="{$website_url}">{$website_url}</a>
</div>
{/if}
{if $about}
<div class="profile_metadata">
	<span class="profile_metadataLabel">About:</span> {$about} 
</div>
{/if}
<br/>

{include file="footer.tpl"}
