{include file="header.tpl"}

<h1>Contact us</h1>
<p>Please use this form to send us your questions and comments about this site.<br />
Starred<span class="requiredFieldIndicator">*</span> items are required.</p>
{if count($messages) > 0}
<div class="formError">
	<ul>
	{section name=message loop=$messages}
		<li>{$messages[message]}</li>
	{/section}
	</ul>
</div>
{/if}
<form action="" method="post" accept-charset="utf-8" class="bpsForm">
	<label for="name">Your name</label>	
	<input class="bpsFormInput" type="text" name="name" value="{$name}" id="name"/>

	<label for="email">Your email <span class="requiredFieldIndicator">*</span></label>
	<input class="bpsFormInput" type="text" name="email" value="{$email}" id="email"/>

	<label for="subject">Subject <span class="requiredFieldIndicator">*</span></label>
	<input class="bpsFormInput" type="text" name="subject" value="{$subject}" id="subject" style="width:400px"/>

	<label for="message">Your comment or question <span class="requiredFieldIndicator">*</span></label>
	<textarea class="bpsFormInput" name="message" rows="8" cols="40" style="width:400px">{$message}</textarea>
	{if $objId > 0 }
	<input type="hidden" name="objid" value="{$objId}"/>
	{/if}
	
	<div class="buttonRow">
		<input type="submit" name="submit" value="Submit"/>
	</div>
</form>


{include file="footer.tpl"}
