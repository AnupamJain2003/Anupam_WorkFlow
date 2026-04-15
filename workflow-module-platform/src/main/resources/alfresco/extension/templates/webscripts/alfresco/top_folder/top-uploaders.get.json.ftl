{
  "site": "${site}",
  "topUploaders": [
  <#list topUploaders as user>
    {
      "user": "${user.user}",
      "count": ${user.count}
    }<#if user_has_next>,</#if>
  </#list>
  ]
}